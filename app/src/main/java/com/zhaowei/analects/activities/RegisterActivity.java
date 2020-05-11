package com.zhaowei.analects.activities;

import android.app.ProgressDialog;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.User;
import com.zhaowei.analects.utils.Md5Util;
import com.zhaowei.analects.utils.TextChangedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends BaseActivity {

    private Toolbar toolbar;

    private RadioButton radioButtonOne;
    private ImageView imageViewPwd;
    private ImageView imageViewConfirm;
    private EditText editTextName;
    private EditText editTextPhone;
    private EditText editTextPwd;
    private EditText editTextConfirm;
    private Button buttonRegister;
    private ProgressDialog progressDialog;

    private final int REGISTER_OK = 500;
    private final int REGISTER_FAIL = 501;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REGISTER_OK:
                    //注册成功
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "注册成功！", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case REGISTER_FAIL:
                    //注册失败
                    progressDialog.dismiss();
                    new MaterialDialog.Builder(RegisterActivity.this)
                            .title("注册失败！")
                            .content("请检查您的账号名和手机号码！")
                            .positiveText("好的")
                            .cancelable(true)
                            .show();
                    break;
                default:
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "注册失败！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar = (Toolbar) findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar);

        radioButtonOne = (RadioButton)findViewById(R.id.radio_one);
        imageViewPwd = (ImageView) findViewById(R.id.image_view_register_pwd);
        imageViewConfirm = (ImageView) findViewById(R.id.image_view_register_confirmpwd);
        editTextName = (EditText) findViewById(R.id.edit_register_user_name);
        editTextPhone = (EditText) findViewById(R.id.edit_register_user_phone);
        editTextPwd = (EditText) findViewById(R.id.edit_register_user_pwd);
        editTextConfirm = (EditText) findViewById(R.id.edit_register_confim_user_pwd);
        buttonRegister = (Button) findViewById(R.id.btn_register_user);

        TextChangedListener.StringWatcher(editTextPwd);
        TextChangedListener.StringWatcher(editTextConfirm);

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

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String icon = "";
                if (radioButtonOne.isChecked()){
                    icon = "1";
                }else{
                    icon = "2";
                }
                final String choice = icon;
                String name = editTextName.getText().toString();
                String phone = editTextPhone.getText().toString();
                String pwd = editTextPwd.getText().toString();
                String confirm = editTextConfirm.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "账号不能为空！", Toast.LENGTH_SHORT).show();
                } else if (!isMobileNO(phone)){
                    Toast.makeText(getApplicationContext(), "手机号码不正确！", Toast.LENGTH_SHORT).show();
                }else if (pwd.equals("")) {
                    Toast.makeText(getApplicationContext(), "密码不能为空！", Toast.LENGTH_SHORT).show();
                } else if (!pwd.equals(confirm)) {
                    Toast.makeText(getApplicationContext(), "两次密码不同！", Toast.LENGTH_SHORT).show();
                } else {
                    String md5pwd = Md5Util.md5Password(pwd);
                    //联网注册
                    progressDialog = new ProgressDialog(RegisterActivity.this);
                    progressDialog.setTitle("正在注册...");
                    progressDialog.setMessage("请等待...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            register(choice, name, md5pwd, phone);
                        }
                    }, 1000);

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

    //注册
    private void register(final String icon, final String name, final String pwd, final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    RequestBody body = new FormBody.Builder()
                            .add("usericon", icon)//添加键值对
                            .add("username", name)
                            .add("phone", phone)
                            .add("md5pwd", pwd)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://148.70.155.194:8080/WebApplication/register.jsp")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string().trim();
                    try {
                        //获取返回值,500为成功,501为失败
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
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
