package com.zhaowei.analects.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.User;

public class LoginUserActivity extends BaseActivity {

    private Toolbar toolbar;

    private ImageView imageViewUserIcon;

    private TextView textViewUserName;

    private LinearLayout linearLayoutLogout;

    private LinearLayout linearLayoutChange;

    private LinearLayout linearLayoutReset;

    private LinearLayout linearLayoutAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        toolbar = (Toolbar) findViewById(R.id.toolbar_logined);
        setSupportActionBar(toolbar);

        imageViewUserIcon = (ImageView)findViewById(R.id.image_view_logined_icon);
        textViewUserName = (TextView)findViewById(R.id.tv_logined_name);
        linearLayoutLogout = (LinearLayout) findViewById(R.id.ll_logined_logout);
        linearLayoutChange = (LinearLayout) findViewById(R.id.ll_logined_change_pwd);
        linearLayoutReset = (LinearLayout) findViewById(R.id.ll_logined_reset_phone);
        linearLayoutAbout = (LinearLayout) findViewById(R.id.ll_logined_about);

        Intent intent = getIntent();
        User user = (User) intent.getSerializableExtra("ME");

        textViewUserName.setText(user.getName());
        switch (user.getIcon()){
            case 1:
                imageViewUserIcon.setImageResource(R.drawable.ic_boy);
                break;
            case 2:
                imageViewUserIcon.setImageResource(R.drawable.ic_girl);
                break;
        }

        linearLayoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent("com.zhaowei.offline");
                sendBroadcast(intent1);
            }
        });

        linearLayoutChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentChange = new Intent(getApplicationContext(), ChangePwdActivity.class);
                startActivity(intentChange);
            }
        });

        linearLayoutReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReset = new Intent(getApplicationContext(), ResetPhoneActivity.class);
                startActivity(intentReset);
            }
        });

        linearLayoutAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAbout = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intentAbout);
            }
        });
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
