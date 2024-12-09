package com.example.SerraDomotica.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Log.d("FileUtils", "Directory created: " + path);
            } else {
                Log.e("FileUtils", "Failed to create directory: " + path);
            }
        } else {
            Log.d("FileUtils", "Directory already exists: " + path);
        }
    }

    public static void createFileIfNotExists(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    Log.d("FileUtils", "File created: " + path);
                } else {
                    Log.e("FileUtils", "Failed to create file: " + path);
                }
            } catch (IOException e) {
                Log.e("FileUtils", "IOException during file creation: " + e.getMessage());
            }
        } else {
            Log.d("FileUtils", "File already exists: " + path);
        }
    }
}