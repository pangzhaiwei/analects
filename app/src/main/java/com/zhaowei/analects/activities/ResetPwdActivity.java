package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.utils.Md5Util;
import com.zhaowei.analects.utils.TextChangedListener;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetPwdActivity extends BaseActivity {

    private Toolbar toolbar;

    private ImageView imageViewPwd;

    private ImageView imageViewConfirm;

    private EditText editTextPwd;

    private EditText editTextConfirm;

    private Button button;

    private TextView textView;

    private ProgressDialog progressDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (msg.what) {
                case 500:
                    Toast.makeText(getApplicationContext(), "修改成功！", Toast.LENGTH_SHORT).show();
                    //发送下线通知
                    Intent intent = new Intent("com.zhaowei.direct");
                    sendBroadcast(intent);
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "修改失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);

        toolbar = (Toolbar) findViewById(R.id.toolbar_reset_pwd);
        setSupportActionBar(toolbar);

        imageViewPwd = (ImageView) findViewById(R.id.image_view_reset_pwd);
        imageViewConfirm = (ImageView) findViewById(R.id.image_view_reset_confirm);
        editTextPwd = (EditText) findViewById(R.id.edit_reset_user_pwd);
        editTextConfirm = (EditText) findViewById(R.id.edit_reset_confim_user_pwd);
        button = (Button) findViewById(R.id.btn_submit_reset);
        textView = (TextView) findViewById(R.id.tv_show_username);

        TextChangedListener.StringWatcher(editTextPwd);
        TextChangedListener.StringWatcher(editTextConfirm);

        Intent intent = getIntent();
        int userid = intent.getIntExtra("userid", -1);
        String username = intent.getStringExtra("username");

        textView.setText(username);

        imageViewPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPwd.getInputType() == 128){
                    editTextPwd.setInputType(129);
                    imageViewPwd.setImageDrawable(getDrawable(R.drawable.ic_visibility_off_black_24dp));
                }else if (editTextPwd.getInputType() == 129){
                    editTextPwd.setInputType(128);
                    imageViewPwd.setImageDrawable(getDrawable(R.drawable.ic_visibility_black_24dp));
                }
                editTextPwd.setSelection(editTextPwd.getText().length());
            }
        });

        imageViewConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextConfirm.getInputType() == 128){
                    editTextConfirm.setInputType(129);
                    imageViewConfirm.setImageDrawable(getDrawable(R.drawable.ic_visibility_off_black_24dp));
                }else if (editTextConfirm.getInputType() == 129){
                    editTextConfirm.setInputType(128);
                    imageViewConfirm.setImageDrawable(getDrawable(R.drawable.ic_visibility_black_24dp));
                }
                editTextConfirm.setSelection(editTextConfirm.getText().length());
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userid == -1){
                    Toast.makeText(getApplicationContext(), "用户信息有误，请重新验证！", Toast.LENGTH_SHORT).show();
                    return;
                }
                String pwd = editTextPwd.getText().toString();
                String confirm = editTextConfirm.getText().toString();
                if (pwd.equals("")) {
                    Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
                } else if (!pwd.equals(confirm)) {
                    Toast.makeText(getApplicationContext(), "两次密码不同！", Toast.LENGTH_SHORT).show();
                } else {
                    String md5pwd = Md5Util.md5Password(pwd);
                    //联网注册
                    progressDialog = new ProgressDialog(ResetPwdActivity.this);
                    progressDialog.setTitle("正在修改...");
                    progressDialog.setMessage("请等待...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changePwd(userid, md5pwd);
                        }
                    }, 500);

                }
            }
        });

    }

    //修改
    private void changePwd(final int userid, final String pwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("userid", userid+"")
                            .add("md5pwd", pwd)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/changepwd.jsp")
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
