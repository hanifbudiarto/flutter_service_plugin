package com.hanifbudiarto.flutter_service_plugin.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hanifbudiarto.flutter_service_plugin.R;

public class StateIndicatorHelper {

    public static String getStateName(String state) {
        switch (state) {
            case "init":
                return "Initializing";
            case "ready":
                return "Connected";
            case "disconnected":
                return "Disconnected";
            case "sleeping":
                return "Sleep";
            case "lost":
                return "Lost";
            case "alert":
                return "Alert";
        }

        return "Lost";
    }

    public static Bitmap getStateImage(Resources res, String state) {
        switch (state) {
            case "init":
                return BitmapFactory.decodeResource(res, R.drawable.install);
            case "ready":
                return BitmapFactory.decodeResource(res, R.drawable.usb_cable);
            case "disconnected":
                return BitmapFactory.decodeResource(res, R.drawable.disconnected);
            case "sleeping":
                return BitmapFactory.decodeResource(res, R.drawable.dreaming);
            case "lost":
                return BitmapFactory.decodeResource(res, R.drawable.signal);
            case "alert":
                return BitmapFactory.decodeResource(res, R.drawable.warning);
        }

        return BitmapFactory.decodeResource(res, R.drawable.warning);
    }
}
