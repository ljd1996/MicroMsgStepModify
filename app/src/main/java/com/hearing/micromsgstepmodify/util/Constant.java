package com.hearing.micromsgstepmodify.util;

import android.annotation.SuppressLint;

/**
 * @author liujiadong
 * @since 2020/1/13
 */
@SuppressLint("SdCardPath")
public class Constant {

    public static final String TAG = "LLL";

    public static final String MM_DIR = "/data/data/com.tencent.mm/MicroMsg/";
    public static final String TMP_DIR = "/data/data/com.hearing.micromsgstepmodify/";
    public static final String STEP_CFG = "PUSH_stepcounter.cfg";
    public static final String MM_STEP_CFG = "MM_stepcounter.cfg";

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int NEED_LOAD = 2;
    public static final int NEED_ROOT = 3;
}
