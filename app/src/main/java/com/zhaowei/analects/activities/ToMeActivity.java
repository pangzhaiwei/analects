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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.zhaowei.analects.R;
import com.zhaowei.analects.adapters.RVPostAdapter;
import com.zhaowei.analects.adapters.RVReplyAdapter;
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.beans.ReplyPostBean;
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

public class ToMeActivity extends BaseActivity {

    private SwipeRefreshLayout swipeRefreshLayout;

    private Toolbar toolbar;

    private RecyclerView recyclerView;

    private FloatingActionButton floatingActionButton;

    private RVReplyAdapter rvReplyAdapter;

    private final ArrayList<ReplyPostBean> list = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private int userid;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 500:
                    rvReplyAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_me);

        toolbar = (Toolbar)findViewById(R.id.toolbar_reply_to_me);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userid = sharedPreferences.getInt("userid", -1);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_to_me_top);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_reply_to_me);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_reply_to_me);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        floatingActionButton.hide();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearSmoothScroller smoothScroller = new LinearSmoothScroller(ToMeActivity.this) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };
                smoothScroller.setTargetPosition(0);
                recyclerView.getLayoutManager().startSmoothScroll(smoothScroller);
            }
        });

        rvReplyAdapter = new RVReplyAdapter(list, new OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onItemCardClick(View view, int position) {

                //打开
                int postid = list.get(position).getPostid();
                String posttitle = list.get(position).getPosttitle();
                String postcontent = list.get(position).getPostcontent();
                int hereuserid = list.get(position).getUserid();
                String username = list.get(position).getUsername();
                long posttime = list.get(position).getPosttime();
                PostBean postBean = new PostBean(postid, posttitle, postcontent, hereuserid, username, posttime);
                Intent intent = new Intent(ToMeActivity.this, PostInfoActivity.class);
                intent.putExtra("CLICKEDPOST", postBean);
                startActivity(intent);

            }
        });

        recyclerView.setAdapter(rvReplyAdapter);

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
                            refreshReplyToMe(queryid);
                        }
                    }
                }, 500);
            }
        });

        if (userid != -1){
            final String queryid = userid + "";
            refreshReplyToMe(queryid);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshReplyToMe(final String queryid){
        list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("way", 0+"")
                            .add("userid", queryid)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/queryReplyPost.jsp")
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
                            long posttime = jsonObject.getLong("posttime");
                            String replycontent = jsonObject.getString("replycontent");
                            String fromusername = jsonObject.getString("fromusername");
                            String tousername = jsonObject.getString("tousername");
                            long replytime = jsonObject.getLong("replytime");
                            String username = jsonObject.getString("username");

                            ReplyPostBean thisbean = new ReplyPostBean(postid, posttitle, postcontent, userid, username, posttime, replycontent, replytime, fromusername, tousername);
                            list.add(thisbean);
                        }
                        Message message = new Message();
                        message.what = 500;
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
