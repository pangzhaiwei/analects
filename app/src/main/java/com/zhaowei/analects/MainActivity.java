package com.zhaowei.analects;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaowei.analects.activities.BaseActivity;
import com.zhaowei.analects.activities.FromMeActivity;
import com.zhaowei.analects.activities.LoginUserActivity;
import com.zhaowei.analects.activities.MyPostActivity;
import com.zhaowei.analects.activities.NoteActivity;
import com.zhaowei.analects.activities.SearchActivity;
import com.zhaowei.analects.activities.ToMeActivity;
import com.zhaowei.analects.beans.User;
import com.zhaowei.analects.fragments.IndexFragment;
import com.zhaowei.analects.fragments.PostFragment;
import com.zhaowei.analects.fragments.TextFragment;
import com.zhaowei.analects.utils.ArticalDBUtil;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private LinearLayout linearLayoutHeader;

    private TextView textViewUserName;

    private ImageView imageViewUserIcon;

    private Toolbar toolbar;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private NavigationView navigationView;

    private DrawerLayout drawerLayout;

    private ViewPager mViewPager;

    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArticalDBUtil articalDBUtil = new ArticalDBUtil(context);
                try{
                    articalDBUtil.copyDB();
                }catch (IOException e){
                    Log.e("DB copy", "COPY Error");
                }
                Log.d("DB copy", "COPY Done");
            }
        }).start();

        Intent loginIntent = getIntent();
        Bundle bundle = loginIntent.getExtras();
        User user = (User)bundle.getSerializable("ME");

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView)findViewById(R.id.navigation_main_side);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout_main);
        //去掉侧边栏navigation中的滑动条
        navigationView.getChildAt(0).setVerticalScrollBarEnabled(false);

        linearLayoutHeader = navigationView.getHeaderView(0).findViewById(R.id.ll_main_header);
        imageViewUserIcon = navigationView.getHeaderView(0).findViewById(R.id.image_view_user_icon);
        switch (user.getIcon()){
            case 1:
                imageViewUserIcon.setImageResource(R.drawable.ic_boy);
                break;
            case 2:
                imageViewUserIcon.setImageResource(R.drawable.ic_girl);
                break;
        }
        //设置用户名
        textViewUserName = navigationView.getHeaderView(0).findViewById(R.id.tv_user_name);
        textViewUserName.setText(user.getName());
        linearLayoutHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, LoginUserActivity.class);
                intent1.putExtra("ME", user);
                startActivity(intent1);
            }
        });

        setDrawerToggle();

        setListener();

        mViewPager = (ViewPager) findViewById(R.id.mViewPager);
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.mBottom);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_music:
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_text:
                        mViewPager.setCurrentItem(1);
                        break;
                    case R.id.navigation_post:
                        mViewPager.setCurrentItem(2);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        final ArrayList<Fragment> fgList = new ArrayList<>(3);
        fgList.add(new IndexFragment());
        fgList.add(new TextFragment());
        fgList.add(new PostFragment());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                mBottomNavigationView.getMenu().getItem(i).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fgList.get(i);
            }

            @Override
            public int getCount() {
                return fgList.size();
            }
        };

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        Toast.makeText(this, "你拒绝了授权", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "你拒绝了授权", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //程序被关闭，通知音频播放服务关闭
        Log.e("MainActivity", "Destroy()------------------------");
        Intent stopIntent = new Intent("com.zhaowei.stop");
        sendBroadcast(stopIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.icon_search:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting, menu);
        return true;
    }

    /*
    * 设置Drawerlayout的开关,并且和Home图标联动
    * */
    private void setDrawerToggle() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        /*同步drawerlayout的状态*/
        actionBarDrawerToggle.syncState();
    }


    /*
    *设置监听器
    * */
    private void setListener() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.single_user_message:
                        //打开消息页面，别人评论我的，recyclerview，主动获取，下拉刷新
                        Intent intentmyreply = new Intent(MainActivity.this, ToMeActivity.class);
                        startActivity(intentmyreply);
                        break;
                    case R.id.single_user_text:
                        //打开我的贴子页面，依然是recyclerview
                        Intent intentmypost = new Intent(MainActivity.this, MyPostActivity.class);
                        startActivity(intentmypost);
                        break;
                    case R.id.single_user_record:
                        //别人回复我的
                        Intent intentatherreply = new Intent(MainActivity.this, FromMeActivity.class);
                        startActivity(intentatherreply);
                        break;
                    case R.id.single_user_note:
                        Intent intentnote = new Intent(MainActivity.this, NoteActivity.class);
                        startActivity(intentnote);
                        break;
                    case R.id.single_exit:
                        //退出
                        Intent intent = new Intent("com.zhaowei.exit");
                        sendBroadcast(intent);
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

}
