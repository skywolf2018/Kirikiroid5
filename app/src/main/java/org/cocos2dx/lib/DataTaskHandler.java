package org.cocos2dx.lib;

import cz.msebera.android.httpclient.Header;

class DataTaskHandler extends com.loopj.android.http.BinaryHttpResponseHandler {
    private int _taskId;
    private Cocos2dxDownloader _downloader;

    public DataTaskHandler(Cocos2dxDownloader downloader, int taskId) {
        super(new String[]{"application/octet-stream", "text/plain", "audio/mpeg", "image/png", "image/jpeg"});
        this._downloader = downloader;
        this._taskId = taskId;
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        super.onProgress(bytesWritten, totalSize);
        _downloader.onProgress(_downloader._id, _taskId, 0, bytesWritten, totalSize);
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
        _downloader.onFinish(_downloader._id, _taskId, 0, null, binaryData);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
        String errStr = (error != null) ? error.getMessage() : ("HTTP Error " + statusCode);
        _downloader.onFinish(_downloader._id, _taskId, -1, errStr, null);
    }
}