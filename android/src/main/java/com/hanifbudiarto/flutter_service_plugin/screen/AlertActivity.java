package com.hanifbudiarto.flutter_service_plugin.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanifbudiarto.flutter_service_plugin.R;
import com.hanifbudiarto.flutter_service_plugin.model.AppSettings;
import com.hanifbudiarto.flutter_service_plugin.util.DatabaseHelper;
import com.hanifbudiarto.flutter_service_plugin.util.StateIndicatorHelper;

public class AlertActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private TextView tvState;
    private TextView tvDeviceName;
    private TextView tvSerialNumber;
    private ImageView ivState;
    private Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppSettings appSettings = DatabaseHelper.getHelper(this).getAppSettings();

        int themeId = 0;
        try {
            if (appSettings != null) themeId = Integer.parseInt(appSettings.getThemeId());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }


        if (themeId < 10) {
            setContentView(R.layout.activity_alert);
        } else {
            setContentView(R.layout.activity_alert_dark);
        }

        // initialize vibrator
        // Get instance of Vibrator from current Context
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // initialize alarm sound
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
        mediaPlayer.setLooping(true);

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

        tvState = findViewById(R.id.tvState);
        tvDeviceName = findViewById(R.id.tvDeviceName);
        tvSerialNumber = findViewById(R.id.tvSerialNumber);
        ivState = findViewById(R.id.imageState);
        btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, IntentKey.TIME_OUT);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // because single top
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // initialize variable from bundle values
        String value = getIntent().getStringExtra(IntentKey.EXTRA_VALUE); // state polos
        String title = getIntent().getStringExtra(IntentKey.EXTRA_TITLE); // name
        String device = getIntent().getStringExtra(IntentKey.EXTRA_DEVICE); // sn


        tvState.setText(StateIndicatorHelper.getStateName(value));
        tvDeviceName.setText(title);
        tvSerialNumber.setText(device);
        ivState.setImageBitmap(StateIndicatorHelper.getStateImage(getResources(), value));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ringAlarm();
        vibrateAlarm();
    }

    @Override
    protected void onDestroy() {
        try {
            vibrator.cancel();
            mediaPlayer.stop();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        super.onDestroy();
    }

    // keep repeating
    private void vibrateAlarm() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {

                // The '0' here means to repeat indefinitely
                // '0' is actually the index at which the pattern keeps repeating from (the start)
                // To repeat the pattern from any other point, you could increase the index, e.g. '1'
                int repeatVibrate = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                            VibrationEffect.createWaveform(IntentKey.VIBRATE_PATTERN, repeatVibrate)
                    );
                } else {
                    vibrator.vibrate(IntentKey.VIBRATE_PATTERN, repeatVibrate);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void ringAlarm() {
        try {
            mediaPlayer.start();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
