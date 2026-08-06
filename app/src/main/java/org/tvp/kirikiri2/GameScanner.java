package org.tvp.kirikiri2;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;


public class GameScanner {


    private static final String CACHE = "gamecache_v3.dat";


    // 跳过无关目录，提高扫描速度
    private static final String[] IGNORE_DIR = {

            "android",
            "dcim",
            "pictures",
            "movies",
            "music",
            "download",
            "documents"

    };



    /**
     * 扫描游戏
     */
    public static ArrayList<File> scan(File root) {


        ArrayList<File> result = new ArrayList<>();


        if(root == null || !root.exists()) {

            return result;
        }


        HashSet<String> cache = new HashSet<>();


        scanFolder(root,result,cache);


        return result;
    }




    /**
     * 递归扫描
     */
    private static void scanFolder(
            File folder,
            ArrayList<File> list,
            HashSet<String> cache) {


        if(isIgnoreFolder(folder)) {

            return;
        }



        File[] files = folder.listFiles();


        if(files == null) {

            return;
        }



        // 第一层先判断是不是游戏目录
        for(File file : files) {


            if(!file.isDirectory()) {


                String name =
                        file.getName()
                                .toLowerCase();



                if(isGameFile(name)) {


                    String path =
                            folder.getAbsolutePath();



                    if(!cache.contains(path)) {


                        cache.add(path);

                        list.add(folder);

                    }


                    // 找到游戏后停止深入
                    return;
                }
            }
        }



        // 继续扫描子目录
        for(File file : files) {


            if(file.isDirectory()) {


                scanFolder(
                        file,
                        list,
                        cache
                );
            }
        }

    }




    /**
     * 判断游戏入口
     */
    private static boolean isGameFile(String name) {


        return name.equals("data.xp3")
                ||
                name.equals("startup.tjs")
                ||
                name.equals("system.tjs");

    }





    /**
     * 忽略目录
     */
    private static boolean isIgnoreFolder(File folder) {


        String name =
                folder.getName()
                        .toLowerCase();


        for(String s : IGNORE_DIR) {


            if(name.equals(s)) {

                return true;
            }
        }


        return false;
    }






    /**
     * 保存缓存
     */
    public static void saveCache(
            Context c,
            ArrayList<File> games) {



        try {


            File file =
                    new File(
                            c.getFilesDir(),
                            CACHE
                    );



            BufferedWriter bw =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(file),
                                    "UTF-8"
                            )
                    );



            for(File f : games) {


                bw.write(
                        f.getAbsolutePath()
                );


                bw.newLine();

            }


            bw.close();



        }catch(Exception ignored) {


        }

    }





    /**
     * 读取缓存
     */
    public static ArrayList<String> loadCache(
            Context c) {


        ArrayList<String> list =
                new ArrayList<>();



        try {


            File file =
                    new File(
                            c.getFilesDir(),
                            CACHE
                    );



            if(!file.exists()) {


                return list;

            }



            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(file),
                                    "UTF-8"
                            )
                    );



            String line;



            while((line = br.readLine()) != null) {


                if(!line.isEmpty()) {


                    File game =
                            new File(line);



                    // 删除不存在的缓存
                    if(game.exists()) {


                        list.add(line);

                    }

                }

            }



            br.close();



        }catch(Exception ignored) {



        }



        return list;

    }





    /**
     * 获取游戏显示名称
     */
    public static String getGameName(String path) {


        File file =
                new File(path);



        return file.getName();

    }


}