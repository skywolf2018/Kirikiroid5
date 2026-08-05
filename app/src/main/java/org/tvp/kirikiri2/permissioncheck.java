package org.tvp.kirikiri2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

public class permissioncheck extends Activity {
    private static final String TAG = "Kirikiroid2";

    private boolean started = false;          // 是否已启动主界面
    private boolean permissionReady = false;  // 权限是否已获得
    private boolean splashFinished = false;   // 5秒动画是否已播放完

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 显示粒子动画
        setContentView(new ParticleSplashView(this));

        if (!isTaskRoot()) {
            finish();
            return;
        }

        // 1. 开始检查权限（不阻塞界面）
        checkStoragePermission();

        // 2. 固定 5 秒后标记动画完成
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashFinished = true;
                tryStartGame(); // 如果权限也已就绪，则跳转
            }
        }, 5000); // 5000 毫秒 = 5 秒
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从权限设置页返回后，重新检查权限状态
        if (!permissionReady && checkStorageOK()) {
            permissionReady = true;
            tryStartGame();
        }
    }

    /**
     * 检查存储权限是否已授予
     */
    private boolean checkStorageOK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * 检查并申请权限
     */
    private void checkStoragePermission() {
        Log.i(TAG, "Checking storage permission");

        if (checkStorageOK()) {
            permissionReady = true;
            tryStartGame(); // 权限已有，但需等待动画完成
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    100
            );
        } else {
            // 低版本默认有权限
            permissionReady = true;
            tryStartGame();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkStorageOK()) {
            permissionReady = true;
            tryStartGame();
        } else {
            showPermissionError("存储权限被拒绝，请手动授予");
        }
    }

    /**
     * 权限错误提示
     */
    private void showPermissionError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("需要文件访问权限")
                .setMessage(message + "\n\n请前往设置开启“所有文件访问权限”或存储权限。")
                .setPositiveButton("去设置", (d, w) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("退出", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    /**
     * 当动画和权限都就绪时，启动主界面
     */
    private void tryStartGame() {
        if (started) return;
        if (permissionReady && splashFinished) {
            startgame();
        } else {
            Log.i(TAG, "Waiting: permissionReady=" + permissionReady + ", splashFinished=" + splashFinished);
        }
    }

    /**
     * 实际跳转到 Kirikiroid2
     */
    private void startgame() {
        if (started) return;
        started = true;
        Intent intent = new Intent(this, Kirikiroid2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * 在跳转前禁用返回键，避免用户误操作打断流程
     */
    @Override
    public void onBackPressed() {
        // 不执行任何操作，防止在动画期间退出
        if (!started) {
            return;
        }
        super.onBackPressed();
    }
}