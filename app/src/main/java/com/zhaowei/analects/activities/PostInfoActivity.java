package com.zhaowei.analects.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.adapters.RVReplyListAdapter;
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.beans.ReplyBean;
import com.zhaowei.analects.utils.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostInfoActivity extends BaseActivity {

    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private RVReplyListAdapter rvReplyListAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FloatingActionButton floatingActionButton;

    private TextView textViewTitle;

    private TextView textViewCreater;

    private TextView textViewTime;

    private TextView textViewContent;

    private final ArrayList<ReplyBean> list = new ArrayList<>();

    private int userid;

    private PostBean postBean;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 500:
                    rvReplyListAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(getApplicationContext(), "更新完成！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_info);

        toolbar = (Toolbar)findViewById(R.id.toolbar_post_info);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        postBean = (PostBean) intent.getSerializableExtra("CLICKEDPOST");

        floatingActionButton = (FloatingActionButton)findViewById(R.id.fab_reply_add);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加评论
                Intent intent1 = new Intent(PostInfoActivity.this, AddReplyActivity.class);
                intent1.putExtra("ADDPOSTID", postBean.getPostid());
                intent1.putExtra("ADDTOUSERID", postBean.getUserid());
                intent1.putExtra("ADDTOUSERNAME", postBean.getUsername());
                startActivityForResult(intent1, 1);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_post_reply);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_post_info);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        View headerview = LayoutInflater.from(this).inflate(R.layout.layout_header, null);
        textViewTitle = (TextView)headerview.findViewById(R.id.tv_post_info_header_title);
        textViewCreater = (TextView)headerview.findViewById(R.id.tv_post_info_header_creater);
        textViewTime = (TextView)headerview.findViewById(R.id.tv_post_info_header_time);
        textViewContent = (TextView)headerview.findViewById(R.id.tv_post_info_header_content);

        textViewTitle.setText(postBean.getPosttitle());
        textViewCreater.setText(postBean.getUsername());
        String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", postBean.getPosttime()).toString();
        textViewTime.setText(date);
        textViewContent.setText(postBean.getPostcontent());

        rvReplyListAdapter = new RVReplyListAdapter(list, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent replyIntent = new Intent(PostInfoActivity.this, AddReplyActivity.class);
                replyIntent.putExtra("ADDPOSTID", list.get(position).getPostid());
                replyIntent.putExtra("ADDTOUSERID", list.get(position).getFromuserid());
                replyIntent.putExtra("ADDTOUSERNAME", list.get(position).getFromusername());
                startActivityForResult(replyIntent, 1);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //弹出菜单：删除或修改
                PopupMenu popupMenu = new PopupMenu(PostInfoActivity.this,view);
                popupMenu.getMenuInflater().inflate(R.menu.reply_delete_fragment,popupMenu.getMenu());

                //弹出式菜单的菜单项点击事件
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.delete_item_list_reply_fragment:

                                if (list.get(position).getFromuserid() == userid){
                                    deleteReply(list.get(position).getReplyid());
                                    list.remove(position);
                                }else{
                                    Toast.makeText(getApplicationContext(), "不是您发送的评论！", Toast.LENGTH_SHORT).show();
                                }

                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

            @Override
            public void onItemCardClick(View view, int position) {

            }
        });

        rvReplyListAdapter.addHeaderView(headerview);

        recyclerView.setAdapter(rvReplyListAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final String postid = postBean.getPostid()+"";
                        refreshDate(postid);
                    }
                },500);
            }
        });

        final String postid = postBean.getPostid()+"";
        refreshDate(postid);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userid = sharedPreferences.getInt("userid", 0);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1){
            final String postid = postBean.getPostid()+"";
            refreshDate(postid);
        }
    }

    private void refreshDate(final String postid){
        list.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("way", 0+"")
                            .add("postid", postid)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/queryPostReply.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            int replyid = jsonObject.getInt("replyid");
                            String replycontent = jsonObject.getString("replycontent");
                            int postid = jsonObject.getInt("postid");
                            long replytime = jsonObject.getLong("replytime");
                            int fromuserid = jsonObject.getInt("fromuserid");
                            int touserid = jsonObject.getInt("touserid");
                            String fromusername = jsonObject.getString("fromusername");
                            String tousername = jsonObject.getString("tousername");
                            ReplyBean replyBean = new ReplyBean(replyid, replycontent, postid, fromuserid, touserid, replytime, fromusername, tousername);
                            list.add(replyBean);

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

    private void deleteReply(final int delreplyid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("replyid", delreplyid + "")//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/deletePostReply.jsp")
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
