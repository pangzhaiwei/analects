package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.PostBean;
import com.zhaowei.analects.beans.ReplyBean;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddReplyActivity extends BaseActivity {

    private Toolbar toolbar;

    private ProgressDialog progressDialog;

    private EditText editTextReply;

    private Button button;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 502:
                    //结束当前activity
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    setResult(1);
                    finish();
                    Toast.makeText(getApplicationContext(), "发送成功！", Toast.LENGTH_LONG).show();
                    break;
                default:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "发送失败！", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reply);

        toolbar = (Toolbar)findViewById(R.id.toolbar_add_reply);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        int getpostid = intent.getIntExtra("ADDPOSTID", 0);
        int gettouserid = intent.getIntExtra("ADDTOUSERID", 0);
        String tousername = intent.getStringExtra("ADDTOUSERNAME");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int userid = sharedPreferences.getInt("userid", -1);

        editTextReply = (EditText)findViewById(R.id.edit_reply_add);
        editTextReply.setHint("@" + tousername);

        button = (Button)findViewById(R.id.btn_submit_reply);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String replyContent = editTextReply.getText().toString();
                if (replyContent.equals("")){
                    Toast.makeText(getApplicationContext(), "内容不能为空！", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog = new ProgressDialog(AddReplyActivity.this);
                    progressDialog.setTitle("正在发送...");
                    progressDialog.setMessage("请等待...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (userid != -1){
                                final int fromid = userid;
                                submitReplyContent(replyContent, userid, getpostid, gettouserid);
                            }
                        }
                    }, 200);
                }
            }
        });
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

    private void submitReplyContent(final String content, final int userid, final int postid, final int touserid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    ReplyBean replyBean = new ReplyBean(0, content, postid, userid, touserid, System.currentTimeMillis(), "", "");
                    Gson gson = new Gson();
                    String json = gson.toJson(replyBean);
                    RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/createPostReply.jsp")//请求的url
                            .post(requestBody)
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    String responseData = response.body().string().trim();
                    try {
                        //获取返回值,502为成功
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
