package com.km.fusionbook.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

/**
 * Utility class
 */
public class Utils {

    /**
     * Send an intent to call a number
     * @param context The current context
     * @param number The phone number to be called
     */
    public static void callPhoneNumber(Context context, String number) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);
        }
    }

    /**
     * Send an intent to write an email
     * @param context The current context
     * @param addresses An array of email addresses
     */
    public static void writeEmail(Context context, String[] addresses) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Send an intent to find a location on the map
     * @param context The current context
     * @param address The address to search
     */
    public static void seeLocationOnMaps(Context context, String address) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }
}
