package io.seal.swarmwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import io.seal.swarmwear.networking.Foursquare;
import io.seal.swarmwear.service.SearchVenuesService;

import static io.seal.swarmwear.lib.Properties.PreferenceKeys.AUTOMATIC_NOTIFICATIONS;

public class UserPresentReceiver extends BroadcastReceiver {

    private static final String TAG = "UserPresentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        EventManager.trackAndLogEvent(TAG, "user present");

        int googlePlayConnectionResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (googlePlayConnectionResult != ConnectionResult.SUCCESS) {
            trackAndLog("google play services error: " + googlePlayConnectionResult);
            return;
        }

        if (!Foursquare.isLoggedIn(context)) {
            trackAndLog("user not logged in on foursquare");
            return;
        }

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean automaticNotifications = defaultSharedPreferences.getBoolean(AUTOMATIC_NOTIFICATIONS, false);

        if (automaticNotifications) {
            SearchVenuesService.start(context, true, false);
        }

    }

    private void trackAndLog(String action) {
        EventManager.trackAndLogEvent(TAG, action);
    }

}