package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaowei.analects.MainActivity;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.User;
import com.zhaowei.analects.utils.Md5Util;
import com.zhaowei.analects.utils.TextChangedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity {

    private ProgressDialog progressDialog;

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    private ImageView imageView;

    private EditText editTextName;

    private EditText editTextPwd;

    private TextView textViewRegister;

    private TextView textViewResetPwd;

    private Button buttonSubmit;

    private CheckBox checkBox;

    private final int LOGIN_OK = 400;
    private final int LOGIN_FAIL = 401;

    private final List<User> users = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_OK:
                    //验证成功就执行下面的代码
                    User me = new User(users.get(0).getId(), users.get(0).getIcon(), users.get(0).getName(), users.get(0).getMd5pwd());
                    editor = preferences.edit();
                    if (checkBox.isChecked()) {
                        //存放信息到sharepreference
                        editor.putString("md5pwd", me.getMd5pwd());
                        editor.putBoolean("remeber_password", true);
                    } else {
                        editor.clear();
                    }
                    editor.putInt("usericon", me.getIcon());
                    editor.putString("username", me.getName());
                    editor.putInt("userid", me.getId());
                    editor.apply();
                    if (progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_SHORT).show();
                    //启动MainActivity，之后finish该Activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ME", me);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    break;
                case LOGIN_FAIL:
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "登录失败！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageView = (ImageView) findViewById(R.id.image_view_login_pwd);
        editTextName = (EditText) findViewById(R.id.edit_user_name);
        editTextPwd = (EditText) findViewById(R.id.edit_user_pwd);
        textViewRegister = (TextView) findViewById(R.id.tv_login_register);
        textViewResetPwd = (TextView) findViewById(R.id.tv_login_forget_pwd);
        buttonSubmit = (Button) findViewById(R.id.btn_submit_user);
        checkBox = (CheckBox) findViewById(R.id.checkbox_remeber_pwd);

        TextChangedListener.StringWatcher(editTextPwd);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPwd.getInputType() == 128){
                    editTextPwd.setInputType(129);
                    imageView.setImageDrawable(getDrawable(R.drawable.ic_visibility_off_black_24dp));
                }else if (editTextPwd.getInputType() == 129){
                    editTextPwd.setInputType(128);
                    imageView.setImageDrawable(getDrawable(R.drawable.ic_visibility_black_24dp));
                }
                editTextPwd.setSelection(editTextPwd.getText().length());
            }
        });

        boolean isRemeber = preferences.getBoolean("remeber_password", false);
        if (isRemeber) {
            String name = preferences.getString("username", null);
            String md5pwd = preferences.getString("md5pwd", null);
            editTextName.setText(name);
            editTextPwd.setText(md5pwd);
            checkBox.setChecked(true);
            autoLogin(name, md5pwd);
        }

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        textViewResetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ConFirmPhoneActivity.class);
                startActivity(intent);
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String pwd = editTextPwd.getText().toString();

                //加密pwd
                String md5pwd = Md5Util.md5Password(pwd);

                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "账号不能为空！", Toast.LENGTH_SHORT).show();
                } else if (pwd.equals("")) {
                    Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
                } else {
                    //联网验证
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setTitle("正在登录...");
                    progressDialog.setMessage("请等待...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            autoLogin(name, md5pwd);
                        }
                    }, 500);
                }

            }
        });

    }

    //登录
    private void autoLogin(final String name, final String pwd) {
        users.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("username", name)//添加键值对
                            .add("md5pwd", pwd)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/login.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int userid = jsonObject.getInt("userid");
                            int usericon = jsonObject.getInt("usericon");
                            String username = jsonObject.getString("username");
                            String md5pwd = jsonObject.getString("userpwd");
                            User user = new User(userid, usericon, username, md5pwd);
                            users.add(user);
                        }
                        Message message = new Message();
                        if (users.size() == 1) {
                            message.what = LOGIN_OK;
                        } else {
                            message.what = LOGIN_FAIL;
                        }
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
