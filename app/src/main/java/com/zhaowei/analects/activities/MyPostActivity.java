package com.zhaowei.analects.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.adapters.RVPostAdapter;
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.utils.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.View.VISIBLE;

public class MyPostActivity extends BaseActivity {

    private Toolbar toolbar;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;

    private FloatingActionButton floatingActionButton;

    private RVPostAdapter rvPostAdapter;

    private final ArrayList<PostBean> list = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private int userid;

    private final int QUERY_OK = 603;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case QUERY_OK:
                    rvPostAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case 500:
                    if (userid != -1){
                        final String queryid = userid + "";
                        refreshData(queryid);
                    }
                    Toast.makeText(getApplicationContext(), "删除成功！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        toolbar = (Toolbar) findViewById(R.id.toolbar_post_me);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userid = sharedPreferences.getInt("userid", -1);

        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab_post_me_top);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_post_me);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_post_me);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        floatingActionButton.hide();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(MyPostActivity.this) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };
                smoothScroller.setTargetPosition(0);
                recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            }
        });

        rvPostAdapter = new RVPostAdapter(list, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {
                //弹出菜单：删除或修改
                PopupMenu popupMenu = new PopupMenu(MyPostActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.my_post_menu,popupMenu.getMenu());

                //弹出式菜单的菜单项点击事件
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.item_my_post_alert:
                                Intent intent = new Intent(MyPostActivity.this, UpdatePostActivity.class);
                                intent.putExtra("MYPOST", list.get(position));
                                startActivityForResult(intent, 1);
                                break;
                            case R.id.item_my_post_delete:
                                int postid = list.get(position).getPostid();
                                final String deletePost = postid + "";
                                deletePost(deletePost);
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

            @Override
            public void onItemCardClick(View view, int position) {
                //查看
                Intent intent = new Intent(MyPostActivity.this, PostInfoActivity.class);
                intent.putExtra("CLICKEDPOST", list.get(position));
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(rvPostAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && floatingActionButton.getVisibility() == VISIBLE) {
                    floatingActionButton.hide();
                } else if (dy < 0 && floatingActionButton.getVisibility() != VISIBLE) {
                    floatingActionButton.show();
                }
            }
        });

        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (userid != -1){
                            final String queryid = userid + "";
                            refreshData(queryid);
                        }
                    }
                }, 500);
            }
        });

        if (userid != -1){
            final String queryid = userid + "";
            refreshData(queryid);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1){
            if (userid != -1){
                final String queryid = userid + "";
                refreshData(queryid);
            }
        }
    }

    private void refreshData(final String queryid){
        list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("userid", queryid)//添加键值对
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

    private void deletePost(final String postid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("postid", postid)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/deletePost.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string().trim();
                    try {
                        //获取返回值,500为成功
                        int back = Integer.valueOf(responseData);
                        Message message = new Message();
                        message.what = back;
                        handler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
