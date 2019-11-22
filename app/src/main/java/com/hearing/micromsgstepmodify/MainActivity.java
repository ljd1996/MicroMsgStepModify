package com.hearing.micromsgstepmodify;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.DataOutputStream;

public class MainActivity extends Activity {

    private static final int COUNTER_DIFF = 1000;

    private Button mLoadButton;
    private Button mStoreButton;

    private EditText mCurrentTodayStepEditText;
    private ImageButton mStepIncImageButton;
    private ImageButton mStepDecImageButton;

    private StepData mStepData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upgradeRootPermission(getPackageCodePath());
        init();
    }

    /**
     * @return 应用程序是/否获取Root权限
     */
    private static void upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd="chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {
        mLoadButton = findViewById(R.id.wechat_load_button);
        mStoreButton = findViewById(R.id.wechat_store_button);
        mStepIncImageButton = findViewById(R.id.wechat_add_button);
        mStepDecImageButton = findViewById(R.id.wechat_sub_button);
        mCurrentTodayStepEditText = findViewById(R.id.wechat_current_today_step_edit_text);

        mStepData = new StepData();

        mStepIncImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepData.changeStep(COUNTER_DIFF);
                updateUI();
            }
        });

        mStepDecImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepData.changeStep(-COUNTER_DIFF);
                updateUI();
            }
        });

        mCurrentTodayStepEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    return;
                }
                mStepData.setStep(Integer.valueOf(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = mStepData.load(MainActivity.this);
                if (result == StepData.SUCCESS) {
                    updateUI();
                    Toast.makeText(MainActivity.this, R.string.loaded, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int result = mStepData.store(MainActivity.this);
                if (result == StepData.SUCCESS) {
                    Toast.makeText(MainActivity.this, R.string.stored, Toast.LENGTH_SHORT).show();
                } else if (result == StepData.CANCEL) {
                    Toast.makeText(MainActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
                } else if (result == StepData.NEED_LOAD) {
                    Toast.makeText(MainActivity.this, R.string.need_load, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI() {
        if (mStepData.getStep() >= 0) {
            mCurrentTodayStepEditText.setText(String.valueOf(mStepData.getStep()));
        }
    }
}
