package com.zhaowei.analects.utils;

public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

}
