package io.seal.swarmwear;

import android.app.Application;
import android.text.TextUtils;
import com.crittercism.app.Crittercism;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashInfo;
import fr.nicolaspomepuy.androidwearcrashreport.mobile.CrashReport;

public class PhoneApp extends Application implements CrashReport.IOnCrashListener {

    private static PhoneApp sInstance;
    private Tracker mTracker;

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

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_PROPERTY_ID);
        }
        return mTracker;
    }

    @Override
    public void onCrashReceived(CrashInfo crashInfo) {
        Crittercism.logHandledException(crashInfo.getThrowable());
        CrashReport.getInstance(this).reportToPlayStore(this);
    }

}
