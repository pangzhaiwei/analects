package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.NoteBean;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteEditorActivity extends BaseActivity {

    private Toolbar toolbar;

    private EditText mEditTextTitle;

    private EditText mEditTextContent;

    private Button mButtonSave;

    private Button mButtonCancel;

    private ProgressDialog progressDialog;

    private SharedPreferences sharedPreferences;

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
        setContentView(R.layout.activity_note_editor);

        toolbar = (Toolbar) findViewById(R.id.toolbar_note_editor);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mEditTextTitle = (EditText) findViewById(R.id.edit_note_title);
        mEditTextContent = (EditText) findViewById(R.id.edit_note_content);
        mButtonSave = (Button) findViewById(R.id.btn_edit_note_save);
        mButtonCancel = (Button) findViewById(R.id.btn_edit_note_cancel);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int userid = sharedPreferences.getInt("userid", -1);
                final String title = mEditTextTitle.getText().toString();
                final String content = mEditTextContent.getText().toString();

                //判断标题和内容是否为空，不为空才能保存
                if ("".equals(title) || "".equals(content)) {
                    Toast.makeText(NoteEditorActivity.this, "标题或者内容不能为空", Toast.LENGTH_LONG).show();
                    return;
                }

                //提示保存
                new AlertDialog.Builder(NoteEditorActivity.this)
                        .setTitle("提示框")
                        .setMessage("确定保存笔记吗？")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        progressDialog = new ProgressDialog(NoteEditorActivity.this);
                                        progressDialog.setTitle("正在发送...");
                                        progressDialog.setMessage("请等待...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                sendNote(title, content, userid);
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

    private void sendNote(final String notetitle, final String notecontent, final int userid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    NoteBean note = new NoteBean(0, notetitle, notecontent, System.currentTimeMillis(), userid, "");
                    Gson gson = new Gson();
                    String json = gson.toJson(note);
                    RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/createNote.jsp")//请求的url
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
