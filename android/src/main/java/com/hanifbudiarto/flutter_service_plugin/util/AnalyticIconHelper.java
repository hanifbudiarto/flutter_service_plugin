package com.hanifbudiarto.flutter_service_plugin.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hanifbudiarto.flutter_service_plugin.R;

public class AnalyticIconHelper {

    public static Bitmap getLargeIconBitmap(Resources res, String analyticModel) {
        /*
        Simple Time Series
        Time Series With Bar
        Horizontal Bar
        Rounded Bar
        Candlestick
        Percentage Gauge
        Button
        Switch
        Slider
        LED
        Bell
        Power-button
        * */
        switch (analyticModel.toLowerCase()) {
            case "simple time series":
            case "simple-time-series":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_time_series);
            case "time series with bar":
            case "bar-time-series":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_time_series_with_bar);
            case "horizontal bar":
            case "horizontal-bar":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_horizontal_bar);
            case "rounded bar":
            case "rounded-bar":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_rounded_bar);
            case "candlestick":
            case "candle-stick":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_candlestick);
            case "percentage gauge":
            case "gauge":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_gauge);
            case "button":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_button);
            case "switch":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_switch);
            case "slider":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_slider);
            case "led":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_led);
            case "bell":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_bell);
            case "power-button":
                return BitmapFactory.decodeResource(res, R.mipmap.ic_power_button);
            default:
                return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
        }
    }
}
