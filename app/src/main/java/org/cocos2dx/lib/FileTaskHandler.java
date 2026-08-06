package org.cocos2dx.lib;

import cz.msebera.android.httpclient.Header;
import java.io.File;

class FileTaskHandler extends com.loopj.android.http.FileAsyncHttpResponseHandler {
    private int _taskId;
    private Cocos2dxDownloader _downloader;

    public FileTaskHandler(Cocos2dxDownloader downloader, int taskId, File file) {
        super(file);
        this._downloader = downloader;
        this._taskId = taskId;
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        super.onProgress(bytesWritten, totalSize);
        _downloader.onProgress(_downloader._id, _taskId, 0, bytesWritten, totalSize);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, File file) {
        _downloader.onFinish(_downloader._id, _taskId, 0, null, null);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
        String errStr = (throwable != null) ? throwable.getMessage() : ("HTTP Error " + statusCode);
        _downloader.onFinish(_downloader._id, _taskId, -1, errStr, null);
    }
}