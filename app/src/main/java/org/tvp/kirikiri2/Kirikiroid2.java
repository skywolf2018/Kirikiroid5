package org.tvp.kirikiri2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.DeviceLimiter;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.LicenseValidator;
import com.google.android.vending.licensing.NullDeviceLimiter;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;

public class Kirikiroid2 extends KR2Activity {
    private static final String TAG = "Kirikiroid2";
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiQYVk7Nt+zFiElM05HOC7EAf1hyvM82dS+AlTWJbxUhIeUMXjs3g7gRPZGKKxlhIGlqFc2C4cdzlY/hnb82pxkTobpEDMaT25ys0DdzU3t+bAMKS1c3JIjknNrbPq946qL2pSGedlCQZUHsNuzea3yupwVgj/bBAyOrWALLuxuqYhRr1gf8wbTwRQffY200p7auW+K1xD0olyeA5r650/4DVPGBZ6L9YKhpHzsBu4X8aXyGzLR2D1ThRG79Ro9tTLe7S/ZUX3gzMwL87FfDzxgtTzi2wFvMBWdSK/94qPZyujH/HZBWVXqzC3SgFC+nEWI6xp1KW0PAK0A/WQsuDzwIDAQAB";
    static Kirikiroid2 KR2Instance;
    private LicenseChecker mChecker = null;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    static String sSignedData = null;
    static String sSignature = null;
    static int lastResponseCode = -1;
    static String s_postdata = null;
    static ProgressDialog progDiag = null;
    private static final byte[] SALT = { -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89 };

    // UI 组件（需要您根据实际布局调整）
    private ListView gameListView;
    private ArrayAdapter<String> gameAdapter;
    private boolean scanning = false; // 扫描状态标志

    // --- 修复：使用弱引用和静态内部类防止内存泄漏 ---
    private static class SafeHandler extends Handler {
        private final WeakReference<Kirikiroid2> activityReference;

        SafeHandler(Kirikiroid2 activity) {
            super(Looper.getMainLooper());
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Kirikiroid2 activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (activity.progDiag != null && activity.progDiag.isShowing()) {
                activity.progDiag.dismiss();
            }
            if (msg.what != 0) {
                activity.MessageBoxOnLicenseFail(activity.getString(msg.what));
                return;
            }
            Bundle bundle = msg.getData();
            if (bundle == null) {
                return;
            }
            String err = bundle.getString("err");
            if (err != null) {
                activity.MessageBoxOnLicenseFail(err);
                return;
            }
            try {
                byte[] data = bundle.getByteArray("data");
                if (data == null) {
                    return;
                }
                FileOutputStream fos = new FileOutputStream(
                        new File(activity.getFilesDir(), "license.sig")
                );
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, "license save error", e);
            }
        }
    }

    private final SafeHandler handler = new SafeHandler(this);

    /*
     * Android 文件打开入口
     */
    private void handleOpenIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Uri uri = intent.getData();
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && uri != null) {
            String path = uri.getPath();
            if (path != null) {
                Log.i(TAG, "Open game path:" + path);
                getSharedPreferences("KR2", MODE_PRIVATE)
                        .edit()
                        .putString("GAME_PATH", path)
                        .apply();
            }
        }
    }

    private class MyLicenseValidator extends LicenseValidator {
        MyLicenseValidator(Policy policy, DeviceLimiter deviceLimiter, LicenseCheckerCallback callback, int nonce, String packageName, String versionCode) {
            super(policy, deviceLimiter, callback, nonce, packageName, versionCode);
        }

        @Override
        public void verify(PublicKey publicKey, int responseCode, String signedData, String signature) {
            lastResponseCode = responseCode;
            sSignedData = signedData;
            sSignature = signature;
            super.verify(publicKey, responseCode, signedData, signature);
        }
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        @Override
        public void allow(int policyReason) {
            if (!isFinishing()) {
                downloadLicense();
            }
        }

        @Override
        public void dontAllow(int policyReason) {
            if (isFinishing()) {
                return;
            }
            MessageBoxOnLicenseFail("License check failed : " + lastResponseCode);
        }

        @Override
        public void applicationError(int errorCode) {
            MessageBoxOnLicenseFail("License error : " + errorCode);
        }
    }

    void MessageBoxOnLicenseFail(String reason) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(R.string.license_fail)
                .setMessage(reason)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateLicense();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    public void downloadLicense() {
        try {
            s_postdata = "?platform=Android"
                    + "&uid=" + URLEncoder.encode(getKR2DeviceId(), "UTF-8")
                    + "&data=" + URLEncoder.encode(sSignedData, "UTF-8")
                    + "&sign=" + URLEncoder.encode(sSignature, "UTF-8")
                    + "&timestamp=" + (new Date().getTime());
            progDiag = ProgressDialog.show(this, "please wait", "downloading", true, false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.what = 0;
                    try {
                        URLConnection con = new URL("https://raw.githubusercontent.com/zeas2/Kirikiroid2_patch/master/js/active.url")
                                .openConnection();
                        BufferedInputStream input = new BufferedInputStream(con.getInputStream());
                        byte[] buffer = new byte[4096];
                        int length = input.read(buffer);
                        input.close();
                        if (length <= 0) {
                            msg.what = R.string.license_data_damaged;
                            handler.sendMessage(msg);
                            return;
                        }
                        String url = new String(buffer, 0, length, "UTF-8");
                        URLConnection con2 = new URL(url + s_postdata).openConnection();
                        BufferedInputStream input2 = new BufferedInputStream(con2.getInputStream());
                        int size = input2.read(buffer);
                        input2.close();
                        if (size <= 0) {
                            msg.what = R.string.license_data_damaged;
                            handler.sendMessage(msg);
                            return;
                        }
                        String result = new String(buffer, 0, size, "UTF-8");
                        Bundle data = new Bundle();
                        if (!result.startsWith("-- SIGNATURE - SHA256/PSS/RSA --")) {
                            data.putString("err", result);
                        } else {
                            data.putByteArray("data", buffer);
                        }
                        msg.setData(data);
                    } catch (Exception e) {
                        msg.what = R.string.recheck_network;
                        Bundle b = new Bundle();
                        b.putString("err", e.getMessage());
                        msg.setData(b);
                    }
                    handler.sendMessage(msg);
                }
            }).start();
        } catch (Exception e) {
            MessageBoxOnLicenseFail(e.getMessage());
        }
    }

    /**
     * 更新游戏列表（UI 线程）
     */
    private void updateGameList(ArrayList<String> gamePaths) {
        if (gameAdapter != null) {
            gameAdapter.clear();
            gameAdapter.addAll(gamePaths);
            gameAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "gameAdapter not initialized, game count: " + gamePaths.size());
        }
        // 可在此处显示“扫描完成”提示
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KR2Instance = this;

        // ----- 设置 UI （您需要提供 layout）-----
        // setContentView(R.layout.main); // 假设布局中有 id="@+id/gameList"
        // gameListView = findViewById(R.id.gameList);
        // gameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        // gameListView.setAdapter(gameAdapter);

        // 临时：如果无法设置布局，可只打印日志
        Log.d(TAG, "onCreate called");

        handleOpenIntent();

        // ----- 异步扫描游戏 -----
        // 1. 首先从缓存快速加载（如果有）
        ArrayList<String> cachedGames = GameScanner.loadCache(this);
        if (!cachedGames.isEmpty()) {
            updateGameList(cachedGames);
        }

        // 2. 启动后台扫描（只执行一次）
        if (!scanning) {
            scanning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<File> games = GameScanner.scan(Environment.getExternalStorageDirectory());
                    // 保存到缓存
                    GameScanner.saveCache(Kirikiroid2.this, games);
                    // 转为路径列表
                    final ArrayList<String> gamePaths = new ArrayList<>();
                    for (File f : games) {
                        gamePaths.add(f.getAbsolutePath());
                    }
                    // 回到主线程更新UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateGameList(gamePaths);
                            scanning = false;
                        }
                    });
                }
            }).start();
        }
        // ----- 异步扫描结束 -----
    }

    // 如果需要，可以覆盖 onDestroy 等生命周期方法
}