package com.hearing.micromsgstepmodify;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hearing.micromsgstepmodify.business.StepHelper;
import com.hearing.micromsgstepmodify.util.Constant;


public class MainActivity extends Activity {

    private static final int COUNTER_DIFF = 1000;

    private EditText mStepEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        Button loadButton = findViewById(R.id.wechat_load_button);
        Button storeButton = findViewById(R.id.wechat_store_button);
        ImageButton stepIncImageButton = findViewById(R.id.wechat_add_button);
        ImageButton stepDecImageButton = findViewById(R.id.wechat_sub_button);
        mStepEditText = findViewById(R.id.wechat_current_today_step_edit_text);

        stepIncImageButton.setOnClickListener(v -> {
            StepHelper.getInstance().changeStep(COUNTER_DIFF);
            updateUI();
        });

        stepDecImageButton.setOnClickListener(v -> {
            StepHelper.getInstance().changeStep(-COUNTER_DIFF);
            updateUI();
        });

        mStepEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    return;
                }
                StepHelper.getInstance().setStep(Integer.valueOf(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        loadButton.setOnClickListener(view -> StepHelper.getInstance().load(MainActivity.this, this::handleCode));

        storeButton.setOnClickListener(view -> StepHelper.getInstance().store(MainActivity.this, this::handleCode));
    }

    private void handleCode(int code) {
        switch (code) {
            case Constant.SUCCESS:
                updateUI();
                Toast.makeText(MainActivity.this, R.string.loaded, Toast.LENGTH_SHORT).show();
                break;
            case Constant.FAIL:
                Toast.makeText(MainActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                break;
            case Constant.NEED_LOAD:
                Toast.makeText(MainActivity.this, R.string.need_load, Toast.LENGTH_SHORT).show();
                break;
            case Constant.NEED_ROOT:
                Toast.makeText(MainActivity.this, R.string.need_root, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void updateUI() {
        if (StepHelper.getInstance().getStep() >= 0) {
            mStepEditText.setText(String.valueOf(StepHelper.getInstance().getStep()));
        }
    }
}
