package io.seal.swarmwear.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.octo.android.robospice.SpiceManager;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.networking.Foursquare;
import io.seal.swarmwear.networking.FoursquareRetrofitSpiceService;

public abstract class BaseSpiceManagerService extends Service {

    private static final String TAG = "BaseSpiceManagerService";

    private SpiceManager mSpiceManager = new SpiceManager(FoursquareRetrofitSpiceService.class);
    private NotificationManagerCompat mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!Foursquare.isLoggedIn(this)) {
            EventManager.handleEvent(TAG, "user not logged in", Log.WARN);
        }

        mSpiceManager.start(this);
        mNotificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mSpiceManager.shouldStop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected SpiceManager getSpiceManager() {
        return mSpiceManager;
    }

    protected NotificationManagerCompat getNotificationManager() {
        return mNotificationManager;
    }
}
