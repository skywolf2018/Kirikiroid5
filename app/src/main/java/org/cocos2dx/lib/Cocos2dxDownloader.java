package org.cocos2dx.lib;

import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;  // ← 新增导入
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Cocos2dxDownloader {
    private static final String TAG = "Cocos2dxDownloader";
    private AsyncHttpClient _httpClient;
    private HashMap<Integer, DownloadTask> _taskMap = new HashMap<>();
    int _id;

    public Cocos2dxDownloader(int id) {
        this._id = id;
        this._httpClient = new AsyncHttpClient();
        this._httpClient.setEnableRedirects(true);
        this._httpClient.setMaxRetriesAndTimeout(3, 5000);
    }

    public void createDownloadTask(final int taskId, final String url, final String path, final boolean isResume) {
        final DownloadTask task = new DownloadTask();
        task.taskId = taskId;
        task.url = url;
        task.path = path;
        task.isResume = isResume;

        if (path != null && path.length() > 0) {
            File file = new File(path);
            File dir = file.getParentFile();
            if (!dir.exists()) dir.mkdirs();
            task.handler = new FileTaskHandler(this, taskId, file);
        } else {
            task.handler = new DataTaskHandler(this, taskId);
        }

        Header[] headers = null;
        if (isResume && path != null) {
            File file = new File(path);
            if (file.exists()) {
                headers = new Header[]{new BasicHeader("Range", "bytes=" + file.length() + "-")};
            }
        }

        _taskMap.put(taskId, task);
        try {
            task.handle = _httpClient.get(Cocos2dxHelper.getActivity(), url, headers, null, (AsyncHttpResponseHandler) task.handler);
        } catch (Exception e) {
            Log.e(TAG, "createDownloadTask failed: " + e.getMessage());
        }
    }

    public void cancelAllRequests() {
        for (Map.Entry<Integer, DownloadTask> entry : _taskMap.entrySet()) {
            DownloadTask task = entry.getValue();
            if (task.handle != null) task.handle.cancel(true);  // ← 现在能正确调用 cancel()
        }
        _taskMap.clear();
    }

    public void cancelRequest(int taskId) {
        DownloadTask task = _taskMap.get(taskId);
        if (task != null && task.handle != null) {
            task.handle.cancel(true);  // ← 现在能正确调用 cancel()
            _taskMap.remove(taskId);
        }
    }

    // ← 去掉 private，改为包级可见（同包的 Handler 类可以访问）
    native void onProgress(int id, int taskId, long dl, long dlnow, long dltotal);
    native void onFinish(int id, int taskId, int errCode, String errStr, byte[] data);

    static class DownloadTask {
        int taskId;
        String url;
        String path;
        boolean isResume;
        Object handler;
        RequestHandle handle;  // ← 从 Object 改为 RequestHandle
    }
}