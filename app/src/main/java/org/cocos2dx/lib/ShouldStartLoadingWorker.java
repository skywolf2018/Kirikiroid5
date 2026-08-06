package org.cocos2dx.lib;

import java.util.concurrent.CountDownLatch;

/* JADX INFO: compiled from: Cocos2dxWebView.java */
/* JADX INFO: loaded from: classes.dex */
class ShouldStartLoadingWorker implements Runnable {
    private CountDownLatch mLatch;
    private boolean[] mResult;
    private final String mUrlString;
    private final int mViewTag;

    ShouldStartLoadingWorker(CountDownLatch latch, boolean[] result, int viewTag, String urlString) {
        this.mLatch = latch;
        this.mResult = result;
        this.mViewTag = viewTag;
        this.mUrlString = urlString;
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mResult[0] = Cocos2dxWebViewHelper._shouldStartLoading(this.mViewTag, this.mUrlString);
        this.mLatch.countDown();
    }
}
