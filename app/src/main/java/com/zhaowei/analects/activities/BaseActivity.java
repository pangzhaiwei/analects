package com.zhaowei.analects.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhaowei.analects.R;
import com.zhaowei.analects.utils.ActivityCollector;

public class BaseActivity extends AppCompatActivity {

    private ForceOfflineReceiver forceOfflineReceiver;

    private DirectOfflineReceiver directOfflineReceiver;

    private ExitReceiver exitReceiver;

    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.zhaowei.offline");
        forceOfflineReceiver = new ForceOfflineReceiver();
        registerReceiver(forceOfflineReceiver, intentFilter);

        IntentFilter intentFilterDirect = new IntentFilter();
        intentFilterDirect.addAction("com.zhaowei.direct");
        directOfflineReceiver = new DirectOfflineReceiver();
        registerReceiver(directOfflineReceiver, intentFilterDirect);

        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction("com.zhaowei.exit");
        exitReceiver = new ExitReceiver();
        registerReceiver(exitReceiver, intentFilter1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (forceOfflineReceiver != null){
            unregisterReceiver(forceOfflineReceiver);
            forceOfflineReceiver = null;
        }
        if (directOfflineReceiver != null){
            unregisterReceiver(directOfflineReceiver);
            directOfflineReceiver = null;
        }
        if (exitReceiver != null){
            unregisterReceiver(exitReceiver);
            exitReceiver = null;
        }
    }

    class ForceOfflineReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("警告！");
            builder.setMessage("即将注销!");
            builder.setCancelable(true);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    ActivityCollector.finishAll();
                    Intent intent1 = new Intent(context, LoginActivity.class);
                    context.startActivity(intent1);
                }
            });
            builder.show();
        }
    }

    class DirectOfflineReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("警告！");
            builder.setMessage("需要重新登录！");
            builder.setCancelable(false);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor = preferences.edit();
                    editor.clear();
                    editor.apply();
                    ActivityCollector.finishAll();
                    Intent intent1 = new Intent(context, LoginActivity.class);
                    context.startActivity(intent1);
                }
            });
            builder.show();
        }
    }

    class ExitReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("警告！");
            builder.setMessage("即将退出!");
            builder.setCancelable(true);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCollector.finishAll();
                }
            });
            builder.show();
        }
    }
}
