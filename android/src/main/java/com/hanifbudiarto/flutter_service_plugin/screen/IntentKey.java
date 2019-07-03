package com.hanifbudiarto.flutter_service_plugin.screen;

public class IntentKey {
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DEVICE = "device";

    public static final int TIME_OUT = 30000; // 30 s

    // Start without a delay
    // Vibrate for 100 milliseconds
    // Sleep for 1000 milliseconds
    public static final long[] VIBRATE_PATTERN = new long[]{0, 400, 800, 600, 800, 800, 800, 1000};

}
