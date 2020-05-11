package com.zhaowei.analects.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaowei.analects.R;
import com.zhaowei.analects.activities.PostEditActivity;
import com.zhaowei.analects.activities.PostInfoActivity;
import com.zhaowei.analects.adapters.RVPostAdapter;
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.utils.OnItemClickListener;
import com.zhaowei.analects.view.MultiFloatingActionButton;
import com.zhaowei.analects.view.TagFabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment {

    private RecyclerView recyclerView;

    private MultiFloatingActionButton multiFloatingActionButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RVPostAdapter rvPostAdapter;

    private final ArrayList<PostBean> list = new ArrayList<>();

    private final int QUERY_OK = 600;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == QUERY_OK){
                rvPostAdapter.notifyDataSetChanged();
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    };


    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view_post);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        multiFloatingActionButton = (MultiFloatingActionButton) view.findViewById(R.id.floating_post);
        multiFloatingActionButton.setOnFabItemClickListener(new MultiFloatingActionButton.OnFabItemClickListener() {
            @Override
            public void onFabItemClick(TagFabLayout view, int pos) {
                switch (pos){
                    case 2:
                        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                            @Override
                            protected int getVerticalSnapPreference() {
                                return LinearSmoothScroller.SNAP_TO_START;
                            }
                        };
                        smoothScroller.setTargetPosition(0);
                        recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
                        break;
                    case 3:
                        //添加动态
                        Intent intent = new Intent(getActivity(), PostEditActivity.class);
                        startActivityForResult(intent, 1);
                        break;
                }
            }
        });

        rvPostAdapter = new RVPostAdapter(list, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemCardClick(View view, int position) {
                //查看详情
                Intent intent = new Intent(getContext(), PostInfoActivity.class);
                intent.putExtra("CLICKEDPOST", list.get(position));
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(rvPostAdapter);

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                    }
                }, 500);
            }
        });

        refreshData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1){
            refreshData();
        }
    }

    private void refreshData(){
        list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("userid", "all")//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/queryPost.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int postid = jsonObject.getInt("postid");
                            String posttitle = jsonObject.getString("posttitle");
                            String postcontent = jsonObject.getString("postcontent");
                            int userid = jsonObject.getInt("userid");
                            String username = jsonObject.getString("username");
                            Long posttime = jsonObject.getLong("posttime");
                            PostBean postBean = new PostBean(postid, posttitle, postcontent, userid, username, posttime);
                            list.add(postBean);
                        }
                        Message message = new Message();
                        message.what = QUERY_OK;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
