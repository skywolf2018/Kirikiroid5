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


    private static final int REQUEST_STORAGE = 100;


    // 防止重复进入
    private boolean started = false;


    // 权限状态
    private boolean permissionReady = false;


    // 动画状态
    private boolean splashFinished = false;



    private final Handler handler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);



        // 显示启动动画
        setContentView(
                new ParticleSplashView(this)
        );



        // 防止重复启动Activity
        if(!isTaskRoot()){

            finish();
            return;

        }



        // 检查权限
        checkStoragePermission();




        /*
         * 启动画面最多等待3秒
         * 权限完成后立即进入
         */
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {


                splashFinished = true;

                tryStartGame();

            }

        },3000);


    }





    @Override
    protected void onResume() {


        super.onResume();


        /*
         * 从系统设置回来重新检查权限
         */
        if(checkStorageOK()){


            permissionReady = true;


            tryStartGame();

        }

    }






    /**
     * 判断存储权限
     */
    private boolean checkStorageOK(){


        // Android 11+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){


            return Environment.isExternalStorageManager();


        }


        // Android 6-10
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){


            return checkSelfPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    ==
                    PackageManager.PERMISSION_GRANTED;


        }



        return true;

    }







    /**
     * 请求权限
     */
    private void checkStoragePermission(){


        Log.i(TAG,"Checking storage permission");



        if(checkStorageOK()){


            permissionReady=true;

            tryStartGame();

            return;

        }





        /*
         * Android 11+
         * 所有文件访问权限
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){


            try{


                Intent intent =
                        new Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                        );


                intent.setData(
                        Uri.parse(
                                "package:"
                                        + getPackageName()
                        )
                );


                startActivity(intent);



            }catch(Exception e){


                Intent intent =
                        new Intent(
                                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        );


                startActivity(intent);


            }



        }

        /*
         * Android 6-10
         */
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){



            requestPermissions(

                    new String[]{

                            android.Manifest.permission.READ_EXTERNAL_STORAGE,

                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE

                    },

                    REQUEST_STORAGE

            );

        }

        else{


            permissionReady=true;

            tryStartGame();

        }


    }







    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults){



        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );



        if(checkStorageOK()){


            permissionReady=true;

            tryStartGame();



        }else{


            showPermissionError(
                    "没有文件访问权限"
            );


        }

    }







    /**
     * 权限失败提示
     */
    private void showPermissionError(String msg){


        new AlertDialog.Builder(this)

                .setTitle("需要文件访问权限")

                .setMessage(
                        msg
                                +
                                "\n\n请开启所有文件访问权限"
                )


                .setPositiveButton(
                        "去设置",
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which){


                                Intent intent =
                                        new Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        );


                                intent.setData(
                                        Uri.parse(
                                                "package:"
                                                        +getPackageName()
                                        )
                                );


                                startActivity(intent);


                            }

                        })


                .setNegativeButton(
                        "退出",
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which){


                                finish();


                            }

                        })


                .setCancelable(false)


                .show();


    }








    /**
     * 权限和动画完成后进入主程序
     */
    private void tryStartGame(){



        if(started){

            return;

        }



        if(permissionReady && splashFinished){


            startgame();


        }else{


            Log.i(
                    TAG,
                    "Waiting permission="
                            +permissionReady
                            +" splash="
                            +splashFinished
            );


        }


    }








    /**
     * 启动KRKR2
     */
    private void startgame(){


        if(started){

            return;

        }



        started=true;



        Intent intent =
                new Intent(
                        this,
                        Kirikiroid2.class
                );



        intent.setFlags(

                Intent.FLAG_ACTIVITY_CLEAR_TASK

                        |

                        Intent.FLAG_ACTIVITY_NEW_TASK

        );



        startActivity(intent);



        finish();


    }








    @Override
    protected void onDestroy(){


        super.onDestroy();


        // 清理Handler
        handler.removeCallbacksAndMessages(null);


    }






    @Override
    public void onBackPressed(){


        // 启动期间禁止返回

        if(!started){


            return;


        }


        super.onBackPressed();


    }



}