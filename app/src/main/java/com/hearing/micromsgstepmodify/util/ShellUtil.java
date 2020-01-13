package com.hearing.micromsgstepmodify.util;

import android.text.TextUtils;

import eu.chainfire.libsuperuser.Shell;


/**
 * @author liujiadong
 * @since 2020/1/13
 */
public class ShellUtil {

    public static void cp(String src, String des) {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(des)) {
            return;
        }

        Shell.SU.run("cp -f " + src + " " + des);
    }
}
