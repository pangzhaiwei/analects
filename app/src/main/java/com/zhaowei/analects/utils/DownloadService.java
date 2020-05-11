package com.zhaowei.analects.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.zhaowei.analects.R;
import com.zhaowei.analects.beans.MusicInfo;
import com.zhaowei.analects.databases.AudioDBHelper;

public class DownloadService extends Service {

    private DownloadTask downloadTask;

    private MusicInfo musicInfo;

    private NotificationChannel notificationChannel;

    private NotificationManager notificationManager;

    private static final int DOWNLOAD_NOTIFICATION_ID = 100;

    private static final String DOWNLOAD_CHANNEL_ID = "com.zhaowei.cn";

    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, getNotification("正在下载....", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //下载完成进行相关逻辑操作
            AudioDBHelper audioDBHelper = new AudioDBHelper(DownloadService.this, "NowAudios.db", null, 1);
            SQLiteDatabase db = audioDBHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            String filename = musicInfo.getName();
            String path = musicInfo.getPath();
            int part = musicInfo.getPart();
            String filepath = Environment.getExternalStorageDirectory() + "/" + getPackageName() + "/myDownLoad/" + path.substring(path.lastIndexOf("/"));
            contentValues.put("name", filename);
            contentValues.put("path", filepath);
            contentValues.put("part", part);
            db.insert("Audios", null, contentValues);
            stopForeground(true);
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, getNotification("下载成功", -1));
            Toast.makeText(DownloadService.this, "下载成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, getNotification("下载失败", -1));
            Toast.makeText(DownloadService.this, "下载失败", Toast.LENGTH_SHORT).show();
        }
    };

    public DownloadService() {
    }

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public class DownloadBinder extends Binder{
        public void startDownload(MusicInfo song){
            musicInfo = song;
            if (downloadTask == null){
                downloadTask = new DownloadTask(downloadListener, DownloadService.this);
                downloadTask.execute(musicInfo);
                startForeground(DOWNLOAD_NOTIFICATION_ID, getNotification("正在下载...", 0));
                Toast.makeText(DownloadService.this, "正在下载...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Notification getNotification(String title, int progress){
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, "下载通知", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
            builder.setChannelId(DOWNLOAD_CHANNEL_ID);
        }
        builder.setSmallIcon(R.drawable.ic_file_download_black_24dp);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        if (progress >= 0){
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}
