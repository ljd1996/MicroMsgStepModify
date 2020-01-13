package com.hearing.micromsgstepmodify.root;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.hearing.micromsgstepmodify.R;
import com.hearing.micromsgstepmodify.util.Constant;
import com.hearing.micromsgstepmodify.util.ShellUtil;

import java.lang.ref.WeakReference;

import eu.chainfire.libsuperuser.Shell;

/**
 * @author liujiadong
 * @since 2020/1/13
 */
public class RootTask extends AsyncTask<Boolean, Void, Void> {

    private WeakReference<Context> mContext;
    private boolean mSuAvailable;
    private ProgressDialog mProgressDialog;
    private OnRootListener mRootListener;

    public RootTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    public void setRootListener(OnRootListener rootListener) {
        mRootListener = rootListener;
    }

    @Override
    protected void onPreExecute() {
        Context context = mContext.get();
        if (context != null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setTitle(R.string.require_access_title);
            mProgressDialog.setMessage(context.getString(R.string.require_access_message));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Boolean... booleans) {
        mSuAvailable = Shell.SU.available();
        if (mSuAvailable) {
            ShellUtil.cp(Constant.MM_DIR + Constant.STEP_CFG, Constant.TMP_DIR);
            ShellUtil.cp(Constant.MM_DIR + Constant.MM_STEP_CFG, Constant.TMP_DIR);
            Shell.SU.run(new String[]{
                    "chmod 777 " + Constant.TMP_DIR + Constant.MM_STEP_CFG,
                    "chmod 777 " + Constant.TMP_DIR + Constant.STEP_CFG,
                    "chmod 777 " + Constant.TMP_DIR
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void results) {
        mProgressDialog.dismiss();

        if (mRootListener != null) {
            mRootListener.onRoot(mSuAvailable);
        }
    }

    public interface OnRootListener {
        void onRoot(boolean rooted);
    }
}
