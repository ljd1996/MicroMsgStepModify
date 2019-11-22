package com.hearing.micromsgstepmodify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import eu.chainfire.libsuperuser.Shell;

import java.io.*;
import java.util.HashMap;


/**
 * Created by liujiadong on 2018/12/5.
 * 步数数据父类
 */
public class StepData {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int ROOTING = 2;
    public static final int CANCEL = 3;
    public static final int NEED_LOAD = 4;

    private static final int TOO_FAST = 5;
    private static final int TOO_MANY = 6;
    private static final int TOO_MANY_FAST = 7;

    private static final int LAST_UPLOAD_STEP = 4;

    private static final double MAX_STEP_PER_SEC = 5.0;
    private static final int MAX_STEP = 80000;
    private String[] ROOT_CMD;
    private int mLoadButtonId;
    private int mStoreButtonId;
    private long mStep;

    @SuppressLint("SdCardPath")
    private static final String STEP_COUNTER_CFG = "/data/data/com.tencent.mm/MicroMsg/PUSH_stepcounter.cfg";
    @SuppressLint("SdCardPath")
    private static final String MM_STEP_COUNTER_CFG = "/data/data/com.tencent.mm/MicroMsg/MM_stepcounter.cfg";
    private static final String WECHAT = "com.tencent.mm";
    private static final String WECHAT_EX = "com.tencent.mm:exdevice";

    private final File mStepCounterCfgFile;
    private final File mMMStepCounterCfgFile;
    private HashMap<Integer, Object> mStepCounterMap = null;
    private HashMap<Integer, Object> mMMStepCounterMap = null;

    public StepData() {
        mStep = -1;

        mStepCounterCfgFile = new File(STEP_COUNTER_CFG);
        mMMStepCounterCfgFile = new File(MM_STEP_COUNTER_CFG);

        ROOT_CMD = new String[]{
                "chmod o+rw " + mStepCounterCfgFile.getAbsolutePath(),
                "chmod o+rw " + mMMStepCounterCfgFile.getAbsolutePath(),
                "chmod o+x " + mStepCounterCfgFile.getParent(),
                "chmod o+x " + mMMStepCounterCfgFile.getParent()
        };
        mLoadButtonId = R.id.wechat_load_button;
        mStoreButtonId = R.id.wechat_store_button;
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

    public final int load(final Context context) {
        int readResult;
        if (!canRead()) {
            (new RootTask(context)).execute(true);
            return ROOTING;
        }

        readResult = read(context);
        mStep = getLastUploadStep();
        return readResult;
    }

    public final int store(final Context context) {
        int writeResult;
        if (!isLoaded()) {
            return NEED_LOAD;
        }

        if (!canWrite()) {
            (new RootTask(context)).execute(false);
            return ROOTING;
        }

        writeResult = write(context);
        return writeResult;
    }

    private long getLastUploadStep() {
        if (mMMStepCounterMap != null) {
            return (long)  mMMStepCounterMap.get(LAST_UPLOAD_STEP);
        } else {
            return -1;
        }
    }

    private boolean canRead() {
        return mStepCounterCfgFile.canRead() && mMMStepCounterCfgFile.canRead();
    }

    private boolean canWrite() {
        return mStepCounterCfgFile.canWrite() && mMMStepCounterCfgFile.canWrite();
    }

    private boolean isLoaded() {
        return mMMStepCounterMap != null && mStepCounterMap != null;
    }

    private int read(Context context) {
        FileInputStream fis;
        ObjectInputStream ois;

        killWechatProcess(context);
        try {

            fis = new FileInputStream(mStepCounterCfgFile);
            ois = new ObjectInputStream(fis);
            mStepCounterMap = (HashMap<Integer, Object>) ois.readObject();
            ois.close();
            fis.close();

            Log.d("LLL", "mStepCounterMap = " + mStepCounterMap);

            fis = new FileInputStream(mMMStepCounterCfgFile);
            ois = new ObjectInputStream(fis);
            mMMStepCounterMap = (HashMap<Integer, Object>) ois.readObject();

            Log.d("LLL", "mMMStepCounterMap = " + mMMStepCounterMap);

            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return FAIL;
        }
    }

    private int write(Context context) {
        FileOutputStream fos;
        ObjectOutputStream oos;

        if (mStepCounterMap == null) {
            return FAIL;
        }
        try {
            killWechatProcess(context);
            fos = new FileOutputStream(STEP_COUNTER_CFG);
            oos = new ObjectOutputStream(fos);
            String tmp = (String) mStepCounterMap.get(301);
            String[] arr = tmp.split(",");
            StringBuilder v = new StringBuilder();
            long step = getStep();
            for (int i = 0; i < arr.length - 3; i++) {
                v.append(arr[i]).append(",");
            }
            v.append(step).append(",").append(step).append(",").append(step);
            mStepCounterMap.put(301, v.toString());
            oos.writeObject(mStepCounterMap);
            oos.close();
            fos.close();

            fos = new FileOutputStream(MM_STEP_COUNTER_CFG);
            oos = new ObjectOutputStream(fos);
            mMMStepCounterMap.put(4, step);
            oos.writeObject(mMMStepCounterMap);
            oos.close();
            fos.close();
            return SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return FAIL;
        }
    }


    private void killWechatProcess(Context context) {
        ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(WECHAT);
        am.killBackgroundProcesses(WECHAT_EX);
    }

    @SuppressLint("StaticFieldLeak")
    private class RootTask extends AsyncTask<Boolean, Void, Void> {
        private Context mContext = null;
        private boolean mSuAvailable;
        @SuppressWarnings("deprecation")
        private ProgressDialog mProgressDialog = null;
        private AlertDialog mAlertDialog = null;
        private boolean isRead;

        public RootTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            //noinspection deprecation
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(R.string.require_access_title);
            mProgressDialog.setMessage(mContext.getString(R.string.require_access_message));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Boolean... booleans) {
            isRead = booleans[0];
            mSuAvailable = Shell.SU.available();
            if (mSuAvailable) {
                Shell.SU.run(ROOT_CMD);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void results) {
            mProgressDialog.dismiss();

            if (mSuAvailable) {
                if (isRead && canRead()) {
                    try {
                        ((Activity) mContext).findViewById(mLoadButtonId).performClick();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else if (canWrite()) {
                    try {
                        ((Activity) mContext).findViewById(mStoreButtonId).performClick();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.note)
                        .setMessage(R.string.require_failed_message)
                        .setCancelable(true);
                mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
    }
}
