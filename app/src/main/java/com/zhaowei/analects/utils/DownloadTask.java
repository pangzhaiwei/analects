package com.zhaowei.analects.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.zhaowei.analects.beans.MusicInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<MusicInfo, Integer, Integer> {

    public static final int STATE_SUCCESS = 0;
    public static final int STATE_FAILED = 1;

    private DownloadListener downloadListener;

    private int lastProgress;

    private Context context;

    public DownloadTask(DownloadListener downloadListener, Context context) {
        this.downloadListener = downloadListener;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(MusicInfo... musicInfos) {
        InputStream inputStream = null;
        RandomAccessFile randomAccessFile = null;
        File file = null;
        try{
            long downloadedLength = 0;
            String downloadUrl = musicInfos[0].getPath();
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")); //例子http://148.70.155.194:8080/downloads/1.mp3，fileNamw就是/1.mp3
            String directory = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/myDownLoad";
            File fileDir = new File(directory);
            if (!fileDir.exists()){
                //后面createNewFile()要求父级目录必须存在，所以这里直接创建好目录，反正这个目录都是要创建的
                fileDir.mkdirs();
            }
            file = new File(directory + fileName);
            if (file.exists()){
                downloadedLength = file.length();
            }else {
                //为避免后面的那个错误，这里就将文件创建出来
                file.createNewFile();
            }
            long contentLength = getContentLength(downloadUrl);
            if (contentLength == 0){
                return STATE_FAILED;
            }else if (contentLength == downloadedLength){
                return STATE_SUCCESS;
            }
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null){
                inputStream = response.body().byteStream();
                //注意，此处randomAccessFile中，file如果为空是会出错的，FileNotFoundExection,为避免，所以前面创建了
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(downloadedLength);;
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = inputStream.read(b)) != -1){
                    total += len;
                    randomAccessFile.write(b, 0, len);
                    int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                    publishProgress(progress);
                }
                response.body().close();
                return STATE_SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null){
                    inputStream.close();
                }
                if (randomAccessFile != null){
                    randomAccessFile.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return STATE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case STATE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case STATE_FAILED:
                downloadListener.onFailed();
                break;
            default:
                break;
        }
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            Log.e("Download", "contentLength = " + contentLength);
            response.body().close();
            return contentLength;
        }
        return 0;
    }
}
