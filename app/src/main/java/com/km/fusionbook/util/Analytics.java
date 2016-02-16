package com.km.fusionbook.util;

import android.content.Context;
import android.util.Log;

import com.km.fusionbook.R;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

public class Analytics {

    public static void viewedScreen(Context applicationContext, String screenName) {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(applicationContext,
                        applicationContext.getString(R.string.mixpanel_token));
        try {
            JSONObject props = new JSONObject();
            props.put("Screen", screenName);
            mixpanel.track("Viewed screen", props);
            mixpanel.flush();
        } catch (JSONException e) {
            Log.e("Fusion Book", "Mixpanel: Unable to add properties to JSONObject", e);
        }
    }

    public static void action(Context applicationContext, String actionName) {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(applicationContext,
                        applicationContext.getString(R.string.mixpanel_token));
        try {
            JSONObject props = new JSONObject();
            props.put("Action", actionName);
            mixpanel.track("Action from user", props);
            mixpanel.flush();
        } catch (JSONException e) {
            Log.e("Fusion Book", "Mixpanel: Unable to add properties to JSONObject", e);
        }
    }
}
