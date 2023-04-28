package com.example.accessibilitytest.data;

import android.app.smdt.SmdtManager;
import android.os.Environment;

/**
 * FileName: Const
 * Author: huangyuguang
 * Date: 2022/9/30
 * Description:
 */
public class Const {
    public static SmdtManager smdtManager;

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/deyi/download/";
    public static final String CRASH_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/deyi/crashLogs/";
    public static final String LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/deyi/logs/";
    public static final String SCREEN_IMG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/deyi/screenImg/";
}
