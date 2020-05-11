package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConFirmPhoneActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText editText;
    private Button button;
    private ProgressDialog progressDialog;

    private final int CONFIRM_OK = 400;
    private final int CONFIRM_FAIL = 401;

    private final List<User> users = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONFIRM_OK:
                    //验证成功就执行下面的代码
                    if (progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "验证成功！", Toast.LENGTH_SHORT).show();
                    //启动MainActivity，之后finish该Activity
                    Intent intent = new Intent(ConFirmPhoneActivity.this, ConFirmCodeActivity.class);
                    intent.putExtra("phone", editText.getText().toString().trim());
                    intent.putExtra("username", users.get(0).getName());
                    intent.putExtra("userid", users.get(0).getId());
                    startActivity(intent);
                    finish();
                    break;
                case CONFIRM_FAIL:
                    if (progressDialog!=null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), "账号未注册！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_firm_phone);

        toolbar = (Toolbar) findViewById(R.id.toolbar_confirm_phone);
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.edit_confirm_user_phone);
        button = (Button) findViewById(R.id.btn_confirm_phone_first);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = editText.getText().toString().trim();
                if (isMobileNO(phone)){
                    //联网验证
                    progressDialog = new ProgressDialog(ConFirmPhoneActivity.this);
                    progressDialog.setTitle("正在验证...");
                    progressDialog.setMessage("请等待...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            confirmPhone(phone);
                        }
                    }, 300);
                }else{
                    Toast.makeText(getApplicationContext(), "请正确填写手机号码！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new MaterialDialog.Builder(ConFirmPhoneActivity.this)
                .title("提示")
                .content("请先输入您注册时所用的手机号码，进行手机号码的验证！")
                .positiveText("好的")
                .cancelable(true)
                .show();

    }

    public static boolean isMobileNO(String mobileNums) {
        /**
         * 判断字符串是否符合手机号码格式
         * 移动号段: 134,135,136,137,138,139,147,150,151,152,157,158,159,170,178,182,183,184,187,188
         * 联通号段: 130,131,132,145,155,156,170,171,175,176,185,186
         * 电信号段: 133,149,153,170,173,177,180,181,189
         * @param str
         * @return 待检测的字符串
         */
        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";// "[1]"代表下一位为数字可以是几，"[0-9]"代表可以为0-9中的一个，"[5,7,9]"表示可以是5,7,9中的任意一位,[^4]表示除4以外的任何一个,\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(mobileNums))
            return false;
        else
            return mobileNums.matches(telRegex);
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

    //验证
    private void confirmPhone(final String phone) {
        users.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("phone", phone)//添加键值对
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/confirmPhone.jsp")
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
                            String md5pwd = "";
                            User user = new User(userid, usericon, username, md5pwd);
                            users.add(user);
                        }
                        Message message = new Message();
                        if (users.size() == 1) {
                            message.what = CONFIRM_OK;
                        } else {
                            message.what = CONFIRM_FAIL;
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
