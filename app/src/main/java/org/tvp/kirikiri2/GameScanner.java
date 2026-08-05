package org.tvp.kirikiri2;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class GameScanner {

    private static final String CACHE = "gamecache.dat";

    /*
     * 扫描游戏（同步，供后台线程调用）
     */
    public static ArrayList<File> scan(File root) {
        ArrayList<File> result = new ArrayList<>();
        if (root == null || !root.exists()) {
            return result;
        }
        scanFolder(root, result);
        return result;
    }

    private static void scanFolder(File folder, ArrayList<File> list) {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanFolder(file, list);
            } else {
                String name = file.getName().toLowerCase();
                // Kirikiri 游戏入口文件
                if (name.equals("data.xp3") ||
                        name.equals("startup.tjs") ||
                        name.equals("system.tjs")) {
                    list.add(file);
                }
            }
        }
    }

    /*
     * 保存缓存（保存所有游戏目录路径）
     */
    public static void saveCache(Context c, ArrayList<File> games) {
        try {
            File file = new File(c.getFilesDir(), CACHE);
            FileOutputStream out = new FileOutputStream(file);
            for (File f : games) {
                out.write((f.getAbsolutePath() + "\n").getBytes());
            }
            out.close();
        } catch (Exception e) {
            // 忽略异常
        }
    }

    /*
     * 读取缓存
     */
    public static ArrayList<String> loadCache(Context c) {
        ArrayList<String> list = new ArrayList<>();
        try {
            File file = new File(c.getFilesDir(), CACHE);
            if (!file.exists()) {
                return list;
            }
            FileInputStream in = new FileInputStream(file);
            byte[] buf = new byte[(int) file.length()];
            in.read(buf);
            in.close();
            String text = new String(buf);
            for (String s : text.split("\n")) {
                if (!s.isEmpty()) {
                    list.add(s);
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return list;
    }
}