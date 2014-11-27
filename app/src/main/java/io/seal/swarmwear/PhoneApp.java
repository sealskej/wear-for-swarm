package io.seal.swarmwear;

import android.app.Application;
import android.text.TextUtils;
import com.crittercism.app.Crittercism;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashInfo;
import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashReport;

import java.util.HashMap;
import java.util.Map;

public class PhoneApp extends Application implements CrashReport.IOnCrashListener {

    private static PhoneApp sInstance;
    private final Map<TrackerName, Tracker> mTrackers = new HashMap<>();

    public static PhoneApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        if (!BuildConfig.DEBUG && !TextUtils.isEmpty(BuildConfig.CRITTERCISM_APP_ID)) {
            Crittercism.initialize(getApplicationContext(), BuildConfig.CRITTERCISM_APP_ID);
            CrashReport.getInstance(this).setOnCrashListener(this);
        }
    }

    protected synchronized Tracker getDefaultTracker() {
        return getTracker(TrackerName.APP_TRACKER);
    }

    private synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // When dry run is set, hits will not be dispatched, but will still be logged as
            // though they were dispatched.
            analytics.setDryRun(BuildConfig.DEBUG);

            if (trackerId == TrackerName.APP_TRACKER) {
                if (!TextUtils.isEmpty(BuildConfig.GOOGLE_ANALYTICS_PROPERTY_ID)) {
                    Tracker tracker = analytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_PROPERTY_ID);
                    mTrackers.put(trackerId, tracker);
                    Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                            tracker,
                            Thread.getDefaultUncaughtExceptionHandler(),
                            getApplicationContext());

                    // Make myHandler the new default uncaught exception handler.
                    Thread.setDefaultUncaughtExceptionHandler(myHandler);
                }
            } else {
                throw new IllegalArgumentException("Not implemented");
            }

        }

        return mTrackers.get(trackerId);
    }

    @Override
    public void onCrashReceived(CrashInfo crashInfo) {
        Crittercism.logHandledException(crashInfo.getThrowable());
        CrashReport.getInstance(this).reportToPlayStore(this);
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p/>
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    private enum TrackerName {
        APP_TRACKER,
    }
}
