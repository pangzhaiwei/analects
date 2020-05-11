package com.zhaowei.analects.activities;

import android.content.Intent;
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
import com.mob.MobSDK;
import com.zhaowei.analects.R;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ConFirmCodeActivity extends BaseActivity {

    private Toolbar toolbar;
    private EditText editText;
    private Button buttonGet;
    private Button buttonSend;

    private boolean flag;   // 操作是否成功
    private String phoneNumber;         // 电话号码
    private String verificationCode;    // 验证码
    private String username;
    private int userid;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;

            if (result == SMSSDK.RESULT_COMPLETE) {
                // 如果操作成功
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    // 校验验证码，返回校验的手机和国家代码
                    Toast.makeText(ConFirmCodeActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConFirmCodeActivity.this, ResetPwdActivity.class);
                    intent.putExtra("userid", userid);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    // 获取验证码成功，true为智能验证，false为普通下发短信
                    Toast.makeText(ConFirmCodeActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                    // 返回支持发送验证码的国家列表
                }
            } else {
                // 如果操作失败
                if (flag) {
                    Toast.makeText(ConFirmCodeActivity.this, "验证码获取失败，请重新获取", Toast.LENGTH_SHORT).show();
                } else {
                    ((Throwable) data).printStackTrace();
                    Toast.makeText(ConFirmCodeActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_firm_code);

        toolbar = (Toolbar) findViewById(R.id.toolbar_confirm_code);
        setSupportActionBar(toolbar);

        editText = (EditText) findViewById(R.id.edit_confirm_code);
        buttonGet = (Button) findViewById(R.id.btn_get_code);
        buttonSend = (Button) findViewById(R.id.btn_confirm_code);

        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("phone");
        username = intent.getStringExtra("username");
        userid = intent.getIntExtra("userid", -1);

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SMSSDK.getVerificationCode("86", phoneNumber); // 发送验证码给号码的 phoneNumber 的手机
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText())) {
                    if (editText.getText().length() == 4) {
                        verificationCode = editText.getText().toString();
                        SMSSDK.submitVerificationCode("86", phoneNumber, verificationCode);
                        flag = false;
                    } else {
                        Toast.makeText(ConFirmCodeActivity.this, "请输入完整的验证码", Toast.LENGTH_SHORT).show();
                        editText.requestFocus();
                    }
                } else {
                    Toast.makeText(ConFirmCodeActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    editText.requestFocus();
                }
            }
        });

        MobSDK.init(this, "按第三方sdk要求填入的信息", "按第三方sdk要求填入的信息");
        EventHandler eventHandler = new EventHandler(){       // 操作回调
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eventHandler);     // 注册回调接口

        String tips = username + " 你好！";

        new MaterialDialog.Builder(ConFirmCodeActivity.this)
                .title("提示")
                .content(tips)
                .positiveText("OK")
                .cancelable(true)
                .show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();  // 注销回调接口
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
