package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zhaowei.analects.R;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetPhoneActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText editText;
    private Button button;
    private ProgressDialog progressDialog;

    private SharedPreferences sharedPreferences;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            switch (msg.what) {
                case 500:
                    new MaterialDialog.Builder(ResetPhoneActivity.this)
                            .title("提示")
                            .content("修改成功！")
                            .positiveText("好的")
                            .cancelable(false)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    finish();
                                }
                            })
                            .show();
                    break;
                case 600:
                    Toast.makeText(getApplicationContext(), "该手机号码已注册！", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_reset_phone);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar_logined_reset_phone);
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.edit_reset_user_phone);
        button = (Button) findViewById(R.id.btn_submit_reset_phone);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userid = sharedPreferences.getInt("userid", -1);
                String phone = editText.getText().toString().trim();
                if (!isMobileNO(phone)){
                    Toast.makeText(getApplicationContext(), "请输入正确的手机号码！", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog = new ProgressDialog(ResetPhoneActivity.this);
                    progressDialog.setTitle("正在修改...");
                    progressDialog.setMessage("请等待...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetPhone(userid, phone);
                        }
                    }, 500);
                }
            }
        });

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

    //修改
    private void resetPhone(final int userid, final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("userid", userid + "")
                            .add("phone", phone)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/resetPhone.jsp")
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
