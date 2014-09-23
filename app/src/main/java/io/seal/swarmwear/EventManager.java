package io.seal.swarmwear;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import com.crittercism.app.Crittercism;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventManager {

    private static final String TAG = "EventManager";

    public static void trackAndLogEvent(String category, String action) {
        handleEvent(category, action, Log.INFO);
    }

    public static void trackAndLogEvent(String category, String action, String label, Long value) {
        handleEvent(category, action, label, value, Log.INFO);
    }

    /**
     * @see EventManager#handleEvent(String, String, String, Long, int)
     */
    public static void handleEvent(String category, String action, int logPriority) {
        handleEvent(category, action, null, null, logPriority);
    }

    /**
     * @param logPriority track event if logPriority equals higher than Log.INFO
     */
    public static void handleEvent(String category, String action, String label, Long value, int logPriority) {

        List<String> msgList = new ArrayList<>();
        msgList.add(action);
        if (label != null) {
            msgList.add(label);
        }
        if (value != null) {
            msgList.add(value + "");
        }

        String msg = TextUtils.join(", ", msgList);
        Log.println(logPriority, category, msg);

        if (Utils.isPersistentLogEnabled()) {
            try {
                Date now = Calendar.getInstance().getTime();
                Context appContext = PhoneApp.getInstance();
                String date = DateFormat.getDateFormat(appContext).format(now);
                String time = DateFormat.getTimeFormat(appContext).format(now);
                FileUtils.writeStringToFile(Utils.getLogFile(),
                        date + " " + time + " : " + category + " : " + msg + "\n",
                        true);
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        if (!BuildConfig.DEBUG) {
            Crittercism.leaveBreadcrumb(action);
        }

        if (logPriority >= Log.INFO) {
            sendEventToTracker(category, action, label, value, PhoneApp.getInstance().getDefaultTracker());
        }
    }

    private static void sendEventToTracker(String category, String action, String label, Long value, Tracker tracker) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value != null ? value : -1)
                .build());
    }

    public static void trackScreenView(String screenName) {
        if (!BuildConfig.DEBUG) {
            Crittercism.leaveBreadcrumb(screenName);
        }
        sendScreenToTracker(screenName, PhoneApp.getInstance().getDefaultTracker());
    }

    private static void sendScreenToTracker(String screenName, Tracker tracker) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
        tracker.setScreenName(null);
    }

}
