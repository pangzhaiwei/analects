package com.zhaowei.analects.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhaowei.analects.R;
import com.zhaowei.analects.adapters.MyContentAdapter;
import com.zhaowei.analects.beans.Chapter;
import com.zhaowei.analects.utils.JsonParseUtil;
import com.zhaowei.analects.view.CircleColorButton;

import org.w3c.dom.Text;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TextFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences preferences;

    private Toolbar toolbar;

    private ScrollView scrollView;

    private Button buttonBig;

    private Button buttonSmall;

    private static int fontSize = 18;

    private CircleColorButton ccbWhite;

    private CircleColorButton ccbGreen;

    private CircleColorButton ccbDark;

    private SeekBar seekBar;

    private int light = 0;

    private int choice = 0;

    private int color = Color.BLACK;

    private TextView mTextViewContent;

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private ListView lvLeftMenu;

    private String[] lvs = {"学而篇", "为政篇", "八佾篇", "里仁篇",
            "公冶长篇", "雍也篇", "述而篇", "泰伯篇", "子罕篇", "乡党篇",
            "先进篇", "颜渊篇", "子路篇", "宪问篇", "卫灵公篇", "季氏篇",
            "阳货篇", "微子篇", "子张篇", "尧曰篇"};

    private List<Chapter> chapters;

    private String jsonData;

    private MyContentAdapter myContentAdapter;

    public TextFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ccb_white:
                choice = 0;
                changeFramentBackground(choice);
                myContentAdapter.setColor(Color.BLACK);
                myContentAdapter.notifyDataSetChanged();
                break;
            case R.id.ccb_green:
                choice = 1;
                changeFramentBackground(choice);
                myContentAdapter.setColor(Color.BLACK);
                myContentAdapter.notifyDataSetChanged();
                break;
            case R.id.ccb_dark:
                choice = 2;
                changeFramentBackground(choice);
                myContentAdapter.setColor(Color.WHITE);
                myContentAdapter.notifyDataSetChanged();
                break;
        }
    }

    //修改背景
    private void changeFramentBackground(int choice){
        switch (choice){
            case 0:
                //调整按钮选中状态
                ccbWhite.setChecked(true);
                ccbGreen.setChecked(false);
                ccbDark.setChecked(false);

                //设置背景
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorCCBWhite));
                mTextViewContent.setBackgroundColor(getResources().getColor(R.color.colorCCBWhite));
                lvLeftMenu.setBackgroundColor(getResources().getColor(R.color.colorCCBWhite));
                mTextViewContent.setTextColor(getResources().getColor(R.color.colorNormalFont));
                toolbar.setTitleTextColor(getResources().getColor(R.color.colorNormalFont));
                break;
            case 1:
                ccbWhite.setChecked(false);
                ccbGreen.setChecked(true);
                ccbDark.setChecked(false);
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorCCBGreen));
                mTextViewContent.setBackgroundColor(getResources().getColor(R.color.colorCCBGreen));
                lvLeftMenu.setBackgroundColor(getResources().getColor(R.color.colorCCBGreen));
                mTextViewContent.setTextColor(getResources().getColor(R.color.colorNormalFont));
                toolbar.setTitleTextColor(getResources().getColor(R.color.colorNormalFont));
                break;
            case 2:
                ccbWhite.setChecked(false);
                ccbGreen.setChecked(false);
                ccbDark.setChecked(true);
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorCCBDark));
                mTextViewContent.setBackgroundColor(getResources().getColor(R.color.colorCCBDark));
                lvLeftMenu.setBackgroundColor(getResources().getColor(R.color.colorCCBDark));
                mTextViewContent.setTextColor(getResources().getColor(R.color.colorCCBDarkFontLight));
                toolbar.setTitleTextColor(getResources().getColor(R.color.colorCCBDarkFontLight));
                break;
        }
    }

    // 获取系统屏幕亮度
    private int getScreenBrightness() {
        int value = 0;
        ContentResolver cr = getActivity().getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {

        }
        return value;
    }

    // 获取app亮度
    private void changeAppBrightness(int brightness) {
        Window window = getActivity().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = (brightness <= 0 ? 1 : brightness) / 255f;
        window.setAttributes(lp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = (ScrollView)view.findViewById(R.id.scroll_view_text_setting);
        buttonBig = (Button)view.findViewById(R.id.btn_font_big);
        buttonSmall = (Button)view.findViewById(R.id.btn_font_small);
        ccbWhite = (CircleColorButton)view.findViewById(R.id.ccb_white);
        ccbGreen = (CircleColorButton)view.findViewById(R.id.ccb_green);
        ccbDark = (CircleColorButton)view.findViewById(R.id.ccb_dark);
        seekBar = (SeekBar)view.findViewById(R.id.seekbar_light);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar_text);
        mTextViewContent = (TextView) view.findViewById(R.id.tv_text_content);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout_menu);
        lvLeftMenu = (ListView) view.findViewById(R.id.lv_left_menu);

        toolbar.setTitle("学而篇");//设置Toolbar标题

        //读取设置
        preferences = getActivity().getSharedPreferences("text", Context.MODE_PRIVATE);
        fontSize = preferences.getInt("FONTSIZE", 18);
        choice = preferences.getInt("BACKGROUND", 0);
        light = preferences.getInt("LIGHT", getScreenBrightness());
        mTextViewContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        seekBar.setProgress(light);
        changeFramentBackground(choice);
        switch (choice){
            case 0:
            case 1:
                color = Color.BLACK;
                break;
            case 2:
                color = Color.WHITE;
                break;
        }

        //设置项默认不可见
        scrollView.setVisibility(View.GONE);
        buttonBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fontSize = fontSize + 1;
                if(fontSize>50){
                    fontSize = 18;
                }
                mTextViewContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            }
        });
        buttonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fontSize = fontSize - 1;
                if (fontSize<=0){
                    fontSize = 18;
                }
                mTextViewContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            }
        });
        mTextViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scrollView.getVisibility()==View.GONE){
                    scrollView.setVisibility(View.VISIBLE);
                }else{
                    scrollView.setVisibility(View.GONE);
                }
            }
        });
        ccbWhite.setChecked(true);
        ccbWhite.setColor(getResources().getColor(R.color.colorCCBWhite));
        ccbGreen.setColor(getResources().getColor(R.color.colorCCBGreen));
        ccbDark.setColor(getResources().getColor(R.color.colorCCBDark));
        ccbWhite.setOnClickListener(this);
        ccbGreen.setOnClickListener(this);
        ccbDark.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    //改变亮度
                    changeAppBrightness(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.open, R.string.stop) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_more));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //设置菜单列表
        myContentAdapter = new MyContentAdapter(getContext(), lvs, color);
        lvLeftMenu.setAdapter(myContentAdapter);

        Gson gson = new Gson();
        jsonData = JsonParseUtil.getJson("data.json", getContext());
        chapters = gson.fromJson(jsonData, new TypeToken<List<Chapter>>(){}.getType());

        mTextViewContent.setText(chapters.get(0).toString());

        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTextViewContent.setText(chapters.get(position).toString());
                toolbar.setTitle(lvs[position]);
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putInt("FONTSIZE", fontSize);
        editor.putInt("BACKGROUND", choice);
        editor.putInt("LIGHT", seekBar.getProgress());
        editor.commit();
    }

}
