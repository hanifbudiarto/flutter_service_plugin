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
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hanifbudiarto.flutter_service_plugin.R;
import com.hanifbudiarto.flutter_service_plugin.model.AppSettings;
import com.hanifbudiarto.flutter_service_plugin.util.DatabaseHelper;

// alarm activity always vibrate and play a ringtone
public class AlarmActivity extends AppCompatActivity {

    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DEVICE = "device";

    private static final int TIME_OUT = 30000; // 30 s

    // Start without a delay
    // Vibrate for 100 milliseconds
    // Sleep for 1000 milliseconds
    private long[] VIBRATE_PATTERN = {0, 100, 1000};

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        AppSettings appSettings = databaseHelper.getAppSettings();

        int themeId = Integer.parseInt(appSettings.getThemeId());
        if (themeId <10) {
            setContentView(R.layout.activity_alarm);
        }
        else {
            setContentView(R.layout.activity_alarm_dark);
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

        // initialize variable from bundle values
        String value = getIntent().getStringExtra(EXTRA_VALUE);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String device = getIntent().getStringExtra(EXTRA_DEVICE);

        TextView tvValue = findViewById(R.id.tvValue);
        tvValue.setText(value);

        TextView tvDevice = findViewById(R.id.tvDevice);
        tvDevice.setText(device);

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        Button btnClose = findViewById(R.id.btnClose);
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
        }, TIME_OUT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ringAlarm();
        vibrateAlarm();
    }

    @Override
    protected void onDestroy() {
        vibrator.cancel();
        mediaPlayer.stop();
        super.onDestroy();
    }

    // keep repeating
    private void vibrateAlarm() {
        try {
            // The '0' here means to repeat indefinitely
            // '0' is actually the index at which the pattern keeps repeating from (the start)
            // To repeat the pattern from any other point, you could increase the index, e.g. '1'
            int repeatVibrate = 0;
            vibrator.vibrate(VIBRATE_PATTERN, repeatVibrate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ringAlarm() {
        try {
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
