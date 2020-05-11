package com.zhaowei.analects.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.Chapter;
import com.zhaowei.analects.beans.MusicInfo;
import com.zhaowei.analects.databases.AudioDBHelper;
import com.zhaowei.analects.utils.DownloadService;
import com.zhaowei.analects.utils.JsonParseUtil;
import com.zhaowei.analects.view.AlbumView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MusicPlayActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {

    private Toolbar toolbar;

    private MyConnection conn;

    private RelativeLayout relativeLayoutShowControl;
    private RelativeLayout relativeLayoutShowContent;
    private TextView textViewShowContent;
    private Button buttonBackToControl;

    private ImageButton buttonPlay;

    private ImageButton buttonNext;

    private ImageButton buttonPrevious;

    private TextView currentTextView;

    private TextView totalTextView;

    private SeekBar seekBar;

    private SeekBar soundSeekBar;

    private AudioManager audioManager;

    private VolumeReceiver volumeReceiver;

    private TextView textView;

    private AudioPlayerService.MyBinder musicControl;

    private List<MusicInfo> music_list = new ArrayList<>();

    private int position;

    private int fromWhere;

    private static final int UPDATE_PROGRESS = 0;

    private UpdateDurationReceiver updateDurationReceiver;

    private LoadedReceiver loadedReceiver;

    private StateReceiver stateReceiver;

    private PreviousStateReceiver previousStateReceiver;

    private NextStateReceiver nextStateReceiver;

    private Button downloadButton;

    private Button speedButton;

    private AudioDBHelper audioDBHelper;

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection downloadConn;

    private String jsonData;
    private List<Chapter> chapters;

    private String translation;
    private List<Chapter> contents;

    private AlbumView albumView;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //倍速选择
    private String[] speeds = new String[]{"0.5", "1.0", "1.5", "2.0"};

    //使用handler定时更新进度条
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    updateProgress();
                    updateSongText();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        toolbar = (Toolbar) findViewById(R.id.toolbar_music_playctivity);
        setSupportActionBar(toolbar);

        //relativeLayout为默认进去看到的界面内容，textviewShowContent则为默认隐藏的原文内容
        relativeLayoutShowControl = (RelativeLayout)findViewById(R.id.relative_layout_control_part);
        relativeLayoutShowContent = (RelativeLayout)findViewById(R.id.relative_layout_show_content);
        textViewShowContent = (TextView)findViewById(R.id.tv_play_music_show_content);
        buttonBackToControl = (Button)findViewById(R.id.btn_play_music_back_to_control);

        preferences = this.getPreferences(MODE_PRIVATE);
        boolean isfirstTime = preferences.getBoolean("isFirstTime", true);
        if (isfirstTime){
            new MaterialDialog.Builder(MusicPlayActivity.this)
                    .title("提示")
                    .content("在本页面中，点击旋转的图片可以查看当前音频对应的章节的原文和译文。")
                    .positiveText("好的")
                    .cancelable(true)
                    .show();
            editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);
            editor.apply();
        }

        //设置textview的滑动观看
        textViewShowContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        //给textview设置原文
        Gson gson1 = new Gson();
        jsonData = JsonParseUtil.getJson("data.json", this);
        chapters = gson1.fromJson(jsonData, new TypeToken<List<Chapter>>(){}.getType());

        Gson gson2 = new Gson();
        translation = JsonParseUtil.getJson("translation.json", this);
        contents = gson2.fromJson(translation, new TypeToken<List<Chapter>>(){}.getType());

        albumView = (AlbumView)findViewById(R.id.album_view);
        albumView.setPlaying(false);
        albumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutShowControl.setVisibility(View.GONE);
                String str = "原文：\n" + chapters.get(musicControl.getCurrentSong().getPart()).toString()
                        + "\n译文：\n" + contents.get(musicControl.getCurrentSong().getPart()).toString();
                textViewShowContent.setText(str);
                relativeLayoutShowContent.setVisibility(View.VISIBLE);
            }
        });


        buttonBackToControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutShowControl.setVisibility(View.VISIBLE);
                relativeLayoutShowContent.setVisibility(View.GONE);
            }
        });

        downloadButton = (Button) findViewById(R.id.btn_play_music_download);

        speedButton = (Button) findViewById(R.id.btn_play_music_speed);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            speedButton.setVisibility(View.INVISIBLE);
        }

        buttonPlay = (ImageButton) findViewById(R.id.btn_music_play);

        buttonNext = (ImageButton) findViewById(R.id.btn_music_next);

        buttonPrevious = (ImageButton) findViewById(R.id.btn_music_previous);

        currentTextView = (TextView) findViewById(R.id.tv_play_currenttime);

        totalTextView = (TextView) findViewById(R.id.tv_play_totaltime);

        seekBar = (SeekBar) findViewById(R.id.seekbar);

        soundSeekBar = (SeekBar) findViewById(R.id.seekbar_sound);

        textView = (TextView) findViewById(R.id.tv_music_playing);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        music_list = (ArrayList<MusicInfo>) bundle.getSerializable("MUSICS");
        position = intent.getIntExtra("POSITION", 0);
        fromWhere = intent.getIntExtra("WHERE", 0);

        if (fromWhere == 0) {
            textView.setText(music_list.get(position).getName());
        }

        Intent intent3 = new Intent(this, AudioPlayerService.class);
        intent3.putExtras(bundle);
        intent3.putExtra("POSITION", position);
        intent3.putExtra("WHERE", fromWhere);
        conn = new MyConnection();
        //使用混合的方法开启服务，
        startService(intent3);
        bindService(intent3, conn, BIND_AUTO_CREATE);

        //音量控制
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //获取最大音量
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundSeekBar.setMax(maxVolume);
        //获取当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundSeekBar.setProgress(currentVolume);

        soundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //设置系统音量
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    soundSeekBar.setProgress(currentVolume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度条改变
                if (fromUser) {
                    musicControl.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始触摸进度条
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止触摸进度条
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext(v);
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious(v);
            }
        });

        audioDBHelper = new AudioDBHelper(this, "NowAudios.db", null, 1);
        downloadConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                downloadBinder = (DownloadService.DownloadBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent download = new Intent(this, DownloadService.class);
        startService(download);
        bindService(download, downloadConn, BIND_AUTO_CREATE);
        /*
         * 下载按钮的功能实现
         * 先获取当前播放曲目信息，
         * 接着检索是否已存在
         * */
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Android 6.0要动态申请权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(MusicPlayActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MusicPlayActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        doDownload();
                    }
                }else {
                    //Android 6.0以下直接下载
                    doDownload();
                }

            }
        });

        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        doDownload();
                    } else {
                        Toast.makeText(MusicPlayActivity.this, "你拒绝了授权", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MusicPlayActivity.this, "你拒绝了授权", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    //下载按钮点击事件
    private void doDownload() {
        //获取到了当前播放曲目
        MusicInfo musicInfo = musicControl.getCurrentSong();

        //检索指定目录
        boolean downloaded = checkAudioFile(musicInfo.getName());

        //存在就Toast提示，否则下载到指定位置
        if (downloaded) {
            Toast.makeText(MusicPlayActivity.this, "该曲目已下载！", Toast.LENGTH_SHORT).show();
        } else {
            downloadBinder.startDownload(musicInfo);
        }
    }

    //检索指定目录的指定文件是否存在
    private boolean checkAudioFile(String filename) {

        SQLiteDatabase db = audioDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from Audios where name like ?", new String[]{filename});
        if (cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }

    }

    public void showMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(MusicPlayActivity.this);
        popup.inflate(R.menu.speed_choice);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.speed_add_zero:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    musicControl.setSpeedOption(0.5f);
                }
                return true;
            case R.id.speed_add_one:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    musicControl.setSpeedOption(1.0f);
                }
                return true;
            case R.id.speed_add_two:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    musicControl.setSpeedOption(1.5f);
                }
                return true;
            case R.id.speed_add_three:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    musicControl.setSpeedOption(2.0f);
                }
                return true;
        }
        return false;
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

    private class MyConnection implements ServiceConnection {

        //服务启动完成后会进入到这个方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得service中的MyBinder
            musicControl = (AudioPlayerService.MyBinder) service;
            //更新按钮的图标
            updatePlayIcon();
            //设置进度条的最大值
            seekBar.setMax(musicControl.getDuration());
            totalTextView.setText(formatTime(musicControl.getDuration()));
            //设置进度条的进度
            seekBar.setProgress(musicControl.getCurrenPostion());
            currentTextView.setText(formatTime(musicControl.getCurrenPostion()));

            //更新曲目名
            updateSongText();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.music.load.completed");
        loadedReceiver = new LoadedReceiver();
        registerReceiver(loadedReceiver, intentFilter);

        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction("com.notification.play");
        //利用广播优先级属性实现有序广播，先修改图标，再改变播放器状态
        stateFilter.setPriority(100);
        stateReceiver = new StateReceiver();
        registerReceiver(stateReceiver, stateFilter);

        IntentFilter previousState = new IntentFilter("com.notification.previous");
        previousState.setPriority(100);
        previousStateReceiver = new PreviousStateReceiver();
        registerReceiver(previousStateReceiver, previousState);

        IntentFilter nextState = new IntentFilter("com.notification.next");
        nextState.setPriority(100);
        nextStateReceiver = new NextStateReceiver();
        registerReceiver(nextStateReceiver, nextState);

        IntentFilter volumeFilter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        volumeReceiver = new VolumeReceiver();
        registerReceiver(volumeReceiver, volumeFilter);

        IntentFilter updateDurationFilter = new IntentFilter("com.music.duration");
        updateDurationReceiver = new UpdateDurationReceiver();
        registerReceiver(updateDurationReceiver, updateDurationFilter);

        //进入到界面后开始更新进度条
        if (musicControl != null) {
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadedReceiver != null) {
            unregisterReceiver(loadedReceiver);
            loadedReceiver = null;
        }
        if (stateReceiver != null) {
            unregisterReceiver(stateReceiver);
            stateReceiver = null;
        }
        if (previousStateReceiver != null){
            unregisterReceiver(previousStateReceiver);
            previousStateReceiver = null;
        }
        if (nextStateReceiver != null){
            unregisterReceiver(nextStateReceiver);
            nextStateReceiver = null;
        }
        if (volumeReceiver != null) {
            unregisterReceiver(volumeReceiver);
            volumeReceiver = null;
        }
        if (updateDurationReceiver != null) {
            unregisterReceiver(updateDurationReceiver);
            updateDurationReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //退出应用后与service解除绑定
        unbindService(conn);
        unbindService(downloadConn);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止更新进度条的进度
        handler.removeCallbacksAndMessages(null);
    }

    //将时长转为常见形式
    private String formatTime(int length) {

        Date date = new Date(length);//调用Date方法获值

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");//规定需要形式

        String TotalTime = simpleDateFormat.format(date);//转化为需要形式

        return TotalTime;

    }

    //更新进度条
    private void updateProgress() {
        int currenPostion = musicControl.getCurrenPostion();
        seekBar.setProgress(currenPostion);
        currentTextView.setText(formatTime(currenPostion));
        //使用Handler每500毫秒更新一次进度条
        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
    }


    //更新按钮的图标
    public void updatePlayIcon() {
        if (musicControl.isPlaying()) {
            albumView.setPlaying(true);
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle_outline_black_80dp));
            handler.sendEmptyMessage(UPDATE_PROGRESS);
        } else {
            albumView.setPlaying(false);
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_80dp));
        }
    }

    //更新曲目名textview
    public void updateSongText() {
        textView.setText(musicControl.getTitle());
    }

    //调用MyBinder中的play()方法
    public void play() {
        musicControl.play();
        updatePlayIcon();
        updateSongText();
    }

    //调用MyBinder中的playNext()方法
    public void playNext(View view) {
        //与updatePlayIcon相反的原因是这个在修改播放状态前检测的
        if (musicControl.isPlaying()) {
            albumView.setPlaying(false);
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_80dp));
        }
        musicControl.playNext();
        updateSongText();
    }

    //调用MyBinder中的playPrevious()方法
    public void playPrevious(View view) {
        //与updatePlayIcon相反的原因是这个在修改播放状态前检测的
        if (musicControl.isPlaying()) {
            albumView.setPlaying(false);
            buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_80dp));
        }
        musicControl.playPrevious();
        updateSongText();
    }

    //接收来自Mediaplayer加载网络音频完成的广播
    class LoadedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //设置进度条的最大值
            seekBar.setMax(musicControl.getDuration());
            totalTextView.setText(formatTime(musicControl.getDuration()));
            play();
        }
    }

    //接收来自Mediaplayer上一曲下一曲加载完成的广播
    class UpdateDurationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //设置进度条的最大值
            seekBar.setMax(musicControl.getDuration());
            totalTextView.setText(formatTime(musicControl.getDuration()));
            updatePlayIcon();
        }
    }

    //接收来自通知栏的上一曲图标修改
    class PreviousStateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //与updatePlayIcon相反的原因是这个在修改播放状态前检测的
            if (musicControl.isPlaying()) {
                albumView.setPlaying(false);
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_80dp));
            }
        }
    }

    //接收来自通知栏的下一曲图标修改
    class NextStateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //与updatePlayIcon相反的原因是这个在修改播放状态前检测的
            if (musicControl.isPlaying()) {
                albumView.setPlaying(false);
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_80dp));
            }
        }
    }

    //接收来自通知栏的状态修改广播
    class StateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //与updatePlayIcon相反的原因是这个在修改播放状态前检测的
            if (musicControl.isPlaying()) {
                albumView.setPlaying(false);
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle_outline_black_80dp));
            } else {
                albumView.setPlaying(true);
                buttonPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle_outline_black_80dp));
            }
        }
    }

    //接收系统音量修改的广播
    class VolumeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                soundSeekBar.setProgress(currentVolume);
            }
        }
    }
}
