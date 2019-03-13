package com.hanifbudiarto.flutter_service_plugin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmActivity extends AppCompatActivity {

    public static final String EXTRA_TOPIC = "topic";
    public static final String EXTRA_MESSAGE = "message"; // value
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DEVICE = "device";

    private TextView tvMessage, tvTitle, tvDevice;
    private String message, title, device;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        String topic = getIntent().getStringExtra(EXTRA_TOPIC);
        message = getIntent().getStringExtra(EXTRA_MESSAGE);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        device = getIntent().getStringExtra(EXTRA_DEVICE);

        tvMessage = findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        tvDevice = findViewById(R.id.tvDevice);
        tvDevice.setText(device);

        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
