package com.zhaowei.analects.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.MusicInfo;
import com.zhaowei.analects.beans.PlayingNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayerService extends Service {

    private MediaPlayer mediaPlayer;

    private List<MusicInfo> music_list = new ArrayList<>();

    private int position;

    private int list_kind = 0;

    private int last_kind = -1;

    private int now_position = -1;

    private NotificationManager notificationManager;

    private String path = "http://148.70.155.194:8080/downloads/1.mp3";

    private RemoteViews views;

    private NextReceiver nextReceiver;

    private PlayReceiver playReceiver;

    private PreviousReceiver previousReceiver;

    private StopReceiver stopReceiver;

    private final int NOTIFICATION_ID = 111;

    public AudioPlayerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();

        registerReceivers();

        initNotification();

        //自动播放下一首
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                new MyBinder().playNext();
            }
        });

        //防止自动播放下一首时出错而产生回调
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });

        Log.e("服务", "准备播放音乐");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int fromWhere = intent.getIntExtra("WHERE", 0);

        switch (fromWhere) {
            case 0:
                Bundle bundle = intent.getExtras();
                music_list = (ArrayList<MusicInfo>) bundle.getSerializable("MUSICS");
                position = intent.getIntExtra("POSITION", 0);
                list_kind = intent.getIntExtra("LIST_KIND", 0);

                if (last_kind == list_kind && now_position == position) {
                    break;
                }
                now_position = position;
                last_kind = list_kind;

                path = music_list.get(position).getPath();

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();

                try {
                    mediaPlayer.setDataSource(path);
                    //准备资源
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            //异步加载完成，通知MusicPlayActivity开始更新UI和播放
                            sendBroadcast(new Intent("com.music.load.completed"));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void playMusic(int i) {
        path = music_list.get(i).getPath();

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();

        updateNotification(music_list.get(i).getName(), true);

        try {
            mediaPlayer.setDataSource(path);
            //准备资源
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    sendBroadcast(new Intent("com.music.duration"));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //该方法包含关于歌曲的操作
    public class MyBinder extends Binder {

        //判断是否处于播放状态
        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        //播放或暂停歌曲
        public void play() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();

                updateNotification(music_list.get(position).getName(), false);

            } else {
                mediaPlayer.start();

                updateNotification(music_list.get(position).getName(), true);

            }
        }

        //下一曲
        public void playNext() {
            position += 1;
            if (position < music_list.size()) {
                playMusic(position);
            } else {
                position = 0;
                playMusic(position);
            }
            now_position = position;
        }

        //上一曲
        public void playPrevious() {
            position = position - 1;
            if (position < music_list.size() && position >= 0) {
                playMusic(position);
            } else {
                position = music_list.size() - 1;
                playMusic(position);
            }
            now_position = position;
        }

        //控制播放速度，注意要API23以上才有用
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void setSpeedOption(float speed) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
            } else {
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                mediaPlayer.pause();
            }
        }

        //返回曲目名
        public String getTitle() {
            return music_list.get(position).getName();
        }

        //返回正在播放的曲目
        public MusicInfo getCurrentSong() {
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setId(music_list.get(position).getId());
            musicInfo.setName(music_list.get(position).getName());
            musicInfo.setPath(music_list.get(position).getPath());
            musicInfo.setPart(music_list.get(position).getPart());
            return musicInfo;
        }

        //返回正在播放的列表的位置
        public PlayingNode getListPosition() {
            PlayingNode playingNode = new PlayingNode();
            if (list_kind == 0) {
                //在线列表
                playingNode.setListKind(0);
                playingNode.setPosition(position);
            } else if (list_kind == 1) {
                //本地列表
                playingNode.setListKind(1);
                playingNode.setPosition(position);
            } else {
                //在线搜索列表
                playingNode.setListKind(2);
                playingNode.setPosition(position);
            }
            return playingNode;
        }

        //返回歌曲的长度，单位为毫秒
        public int getDuration() {
            return mediaPlayer.getDuration();
        }

        //返回歌曲目前的进度，单位为毫秒
        public int getCurrenPostion() {
            return mediaPlayer.getCurrentPosition();
        }

        //设置歌曲播放的进度，单位为毫秒
        public void seekTo(int mesc) {
            mediaPlayer.seekTo(mesc);
        }

    }

    //开启通知栏
    public void initNotification() {

        //点击通知主体跳转
        Intent intent = new Intent(this, MusicPlayActivity.class);
        intent.putExtra("WHERE", 1);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 自定义布局
        views = new RemoteViews(getPackageName(), R.layout.layout_notification);

        //下一曲
        Intent nextIntent = new Intent("com.notification.next");
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nf_next_imgv, nextPending);

        //播放/暂停
        Intent playIntent = new Intent("com.notification.play");
        PendingIntent playPending = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nf_pause_imgv, playPending);

        //上一曲
        Intent previousIntent = new Intent("com.notification.previous");
        PendingIntent previousPending = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nf_previous_imgv, previousPending);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // 获取NotificationManager实例
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel a;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            a = new NotificationChannel("10086", "音乐播放", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(a);
            builder.setChannelId("10086");
        }

        builder.setSmallIcon(R.drawable.ic_play_circle_outline_black_80dp)
                .setContentTitle("Player")
                .setContentText("Playing")
                .setContentIntent(contentPendingIntent)
                .setContent(views);

        // 前台服务
        startForeground(NOTIFICATION_ID, builder.build());
    }

    //更新通知栏状态
    public void updateNotification(String name, boolean flags) {
        //点击通知主体跳转
        Intent intent = new Intent(this, MusicPlayActivity.class);
        intent.putExtra("WHERE", 1);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 自定义布局
        views = new RemoteViews(getPackageName(), R.layout.layout_notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        if (flags) {
            views.setImageViewResource(R.id.nf_pause_imgv, R.drawable.ic_pause_circle_outline_black_80dp);
            views.setTextViewText(R.id.nf_title_tv, name);
        } else {
            views.setImageViewResource(R.id.nf_pause_imgv, R.drawable.ic_play_circle_outline_black_80dp);
            views.setTextViewText(R.id.nf_title_tv, name);
        }

        //下一曲
        Intent nextIntent = new Intent("com.notification.next");
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nf_next_imgv, nextPending);

        //播放/暂停
        Intent playIntent = new Intent("com.notification.play");
        PendingIntent playPending = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nf_pause_imgv, playPending);

        //上一曲
        Intent previousIntent = new Intent("com.notification.previous");
        PendingIntent previousPending = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nf_previous_imgv, previousPending);

        NotificationChannel a;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            a = new NotificationChannel("10086", "音乐播放", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(a);
            builder.setChannelId("10086");
        }

        builder.setSmallIcon(R.drawable.ic_play_circle_outline_black_80dp)
                .setContentTitle("Player")
                .setContentText("Playing")
                .setContentIntent(contentPendingIntent)
                .setContent(views);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceivers();

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // 取消Notification
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }

    }

    //注册BroadcastReceiver
    public void registerReceivers() {

        //利用广播优先级属性实现有序广播，先修改图标，再改变播放器状态
        IntentFilter nextFilter = new IntentFilter();
        nextFilter.addAction("com.notification.next");
        nextFilter.setPriority(50);
        nextReceiver = new NextReceiver();
        registerReceiver(nextReceiver, nextFilter);

        IntentFilter playFilter = new IntentFilter();
        playFilter.addAction("com.notification.play");
        playFilter.setPriority(50);
        playReceiver = new PlayReceiver();
        registerReceiver(playReceiver, playFilter);

        IntentFilter previousFilter = new IntentFilter();
        previousFilter.addAction("com.notification.previous");
        previousFilter.setPriority(50);
        previousReceiver = new PreviousReceiver();
        registerReceiver(previousReceiver, previousFilter);

        IntentFilter stopFilter = new IntentFilter("com.zhaowei.stop");
        stopReceiver = new StopReceiver();
        registerReceiver(stopReceiver, stopFilter);

    }

    //取消注册
    public void unregisterReceivers() {
        if (nextReceiver != null) {
            unregisterReceiver(nextReceiver);
        }
        if (playReceiver != null) {
            unregisterReceiver(playReceiver);
        }
        if (previousReceiver != null) {
            unregisterReceiver(previousReceiver);
        }
        if (stopReceiver != null) {
            unregisterReceiver(stopReceiver);
        }
    }

    //接收通知栏下一曲广播
    class NextReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            new MyBinder().playNext();
        }
    }

    //接收通知栏播放/暂停广播
    class PlayReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            new MyBinder().play();
        }
    }

    //接收通知栏上一曲广播
    class PreviousReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            new MyBinder().playPrevious();
        }
    }

    //接收程序运行结束广播
    class StopReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 停止服务
            stopSelf();
        }
    }

}
