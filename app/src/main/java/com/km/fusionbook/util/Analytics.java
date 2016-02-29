package com.km.fusionbook.util;

import android.content.Context;
import android.util.Log;

import com.km.fusionbook.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility functions to log actions and events to the analytics distant tool
 */
public class Analytics {

    /**
     * Logs an event of type "Viewed screen"
     * @param applicationContext The application context
     * @param screenNameResId The resource ID for the screen name
     */
    public static void viewedScreen(Context applicationContext, int screenNameResId) {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(applicationContext,
                        applicationContext.getString(R.string.mixpanel_token));
        try {
            JSONObject props = new JSONObject();
            props.put("Screen", applicationContext.getString(screenNameResId));
            mixpanel.track("Viewed screen", props);
            mixpanel.flush();
        } catch (JSONException e) {
            Log.e("Fusion Book", "Mixpanel: Unable to add properties to JSONObject", e);
        }
    }

    /**
     * Logs an action
     * @param applicationContext The application context
     * @param actionNameResId The resource ID for the action name
     */
    public static void action(Context applicationContext, int actionNameResId) {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(applicationContext,
                        applicationContext.getString(R.string.mixpanel_token));
        try {
            JSONObject props = new JSONObject();
            props.put("Action", applicationContext.getString(actionNameResId));
            mixpanel.track("Action from user", props);
            mixpanel.flush();
        } catch (JSONException e) {
            Log.e("Fusion Book", "Mixpanel: Unable to add properties to JSONObject", e);
        }
    }
}
