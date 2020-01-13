package com.hearing.micromsgstepmodify.business;

import android.app.ActivityManager;
import android.content.Context;

import com.hearing.micromsgstepmodify.root.RootTask;
import com.hearing.micromsgstepmodify.util.StepUtil;
import com.hearing.micromsgstepmodify.util.ShellUtil;
import com.hearing.micromsgstepmodify.util.Constant;

import java.util.HashMap;


/**
 * Created by liujiadong on 2018/12/5.
 */
public class StepHelper {

    private static final int LAST_UPLOAD_STEP = 4;

    private boolean mIsLoaded;
    private long mStep;

    private StepHelper() {
        mStep = -1;
        mIsLoaded = false;
    }

    private static class SingleTon {
        private static StepHelper sInstance = new StepHelper();
    }

    public static StepHelper getInstance() {
        return SingleTon.sInstance;
    }

    public long getStep() {
        return mStep;
    }

    public void setStep(long step) {
        mStep = step;
    }

    public void changeStep(long diff) {
        mStep = mStep + diff;
        if (mStep < 0) {
            mStep = 0;
        }
    }

    public final void load(final Context context, OnDataListener listener) {
        if (context == null || listener == null) {
            return;
        }
        RootTask task = new RootTask(context);

        task.setRootListener(rooted -> {
            if (rooted) {
                HashMap<Integer, Object> map = StepUtil.readStep(Constant.TMP_DIR + Constant.MM_STEP_CFG);
                if (map != null) {
                    mStep = (long) map.get(LAST_UPLOAD_STEP);
                    mIsLoaded = true;
                    listener.onFinish(Constant.SUCCESS);
                } else {
                    listener.onFinish(Constant.FAIL);
                }
            } else {
                listener.onFinish(Constant.NEED_ROOT);
            }
        });
        task.execute();
    }

    public final void store(final Context context, OnDataListener listener) {
        if (context == null || listener == null) {
            return;
        }

        if (!mIsLoaded) {
            listener.onFinish(Constant.NEED_LOAD);
            return;
        }

        int code1 = StepUtil.writeStep(Constant.TMP_DIR + Constant.STEP_CFG, mStep);
        int code2 = StepUtil.writeMMStep(Constant.TMP_DIR + Constant.MM_STEP_CFG, mStep);
        if (code1 != Constant.SUCCESS || code2 != Constant.SUCCESS) {
            listener.onFinish(Constant.FAIL);
        } else {
            killWechatProcess(context);
            ShellUtil.cp(Constant.TMP_DIR + Constant.STEP_CFG, Constant.MM_DIR);
            ShellUtil.cp(Constant.TMP_DIR + Constant.MM_STEP_CFG, Constant.MM_DIR);
            listener.onFinish(Constant.SUCCESS);
        }
    }

    private void killWechatProcess(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.killBackgroundProcesses("com.tencent.mm");
        }
    }

    public interface OnDataListener {
        void onFinish(int code);
    }
}
