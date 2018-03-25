package com.km.fusionbook.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Helper class with useful functions for checking runtime permissions
 */
public class PermissionUtils {

    public static final int PERMISSION_REQUEST_CALL = 200;

    /*
     * PHONE CALL PERMISSION
     */

    public static boolean isPhoneCallGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPhoneCallPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CALL_PHONE},
                PERMISSION_REQUEST_CALL);
    }
}
