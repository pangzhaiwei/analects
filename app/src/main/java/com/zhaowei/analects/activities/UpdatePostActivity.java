package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.PostBean;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdatePostActivity extends BaseActivity {

    private ProgressDialog progressDialog;

    private Toolbar toolbar;

    private EditText mEditTextTitle;

    private EditText mEditTextContent;

    private Button mButtonSave;

    private Button mButtonCancel;

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
        setContentView(R.layout.activity_update_post);

        toolbar = (Toolbar) findViewById(R.id.toolbar_post_alert);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        PostBean postBean = (PostBean) intent.getSerializableExtra("MYPOST");

        mEditTextTitle = (EditText) findViewById(R.id.edit_alert_title);
        mEditTextContent = (EditText) findViewById(R.id.edit_alert_content);
        mButtonSave = (Button) findViewById(R.id.btn_alert_post_save);
        mButtonCancel = (Button) findViewById(R.id.btn_alert_post_cancel);

        mEditTextTitle.setText(postBean.getPosttitle());
        mEditTextContent.setText(postBean.getPostcontent());

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String posttitle = mEditTextTitle.getText().toString();
                String postcontent = mEditTextContent.getText().toString();
                //判断标题和内容是否为空，不为空才能保存
                if ("".equals(posttitle) || "".equals(postcontent)) {
                    Toast.makeText(UpdatePostActivity.this, "标题或者内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                //提示保存
                new AlertDialog.Builder(UpdatePostActivity.this)
                        .setTitle("提示框")
                        .setMessage("确定修改帖子吗？")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        progressDialog = new ProgressDialog(UpdatePostActivity.this);
                                        progressDialog.setTitle("正在发送...");
                                        progressDialog.setMessage("请等待...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                final PostBean updatePost = new PostBean(postBean.getPostid(), posttitle, postcontent, postBean.getUserid(),
                                                        postBean.getUsername(), System.currentTimeMillis());
                                                sendPost(updatePost);
                                            }
                                        }, 200);

                                    }
                                }).setNegativeButton("取消", null).show();

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

    private void sendPost(final PostBean postentity) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Gson gson = new Gson();
                    String json = gson.toJson(postentity);
                    RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/updatePost.jsp")//请求的url
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
