package io.seal.swarmwear.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.mariux.teleport.lib.TeleportClient;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import io.seal.swarmwear.BusProvider;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.Utils;
import io.seal.swarmwear.activity.IntroductionActivity;
import io.seal.swarmwear.event.VenuesAvailableEvent;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;
import io.seal.swarmwear.model.search.SearchResponse;
import io.seal.swarmwear.networking.request.SearchRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.AUTOMATIC_NOTIFICATIONS;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.MINIMUM_LOCATION_DISTANCE;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.PASSIVE_LOCATION_UPDATE_INTERVAL;

public class SearchVenuesService extends BaseSpiceManagerService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, RequestListener<SearchResponse> {

    private static final String TAG = "SearchVenuesService";

    public static final String NOTIFICATIONS_DISPATCHER_TREAD = "notifications-dispatcher-tread";
    public static final int MILLIS = 1000;
    private static final long ACTIVE_EXPIRATION_DURATION = 30 * MILLIS;
    public static final int IMAGES_DOWNLOAD_TIMEOUT = 5 * MILLIS;
    private final String VENUES_NOTIFICATION_GROUP = "venues_notification_group";

    private LocationClient mLocationClient;
    private Handler mHandler;
    private LocationRequest mLocationRequest;
    private Handler mActiveSearchExpirationHandler;
    private SharedPreferences mSharedPreferences;
    private Location mLastLocation;
    private boolean mIsPassive;
    private boolean mSendToWearable;
    private TeleportClient mTeleportClient;

    private Runnable mExpirationRunnable = new Runnable() {
        @Override
        public void run() {
            onSearchFinished();
        }
    };

    public static void start(Context context, boolean passive) {
        start(context, passive, true);
    }

    public static void start(Context context, boolean passive,
                             boolean updateLocation) {
        start(context, passive, updateLocation, false);
    }

    public static void start(Context context, boolean passive,
                             boolean updateLocation, boolean sendToWearable) {

        EventManager.trackAndLogEvent(TAG, "starting service");
        Context appContext = context.getApplicationContext();
        Intent intent = new Intent(appContext, SearchVenuesService.class);
        intent.putExtra(Properties.Keys.PASSIVE, passive);
        intent.putExtra(Properties.Keys.UPDATE_LOCATION_REQUEST, updateLocation);
        intent.putExtra(Properties.Keys.SEND_TO_WEARABLE, sendToWearable);
        appContext.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        EventManager.trackAndLogEvent(TAG, "onCreate");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        HandlerThread thread = new HandlerThread(NOTIFICATIONS_DISPATCHER_TREAD, Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        mHandler = new Handler(thread.getLooper());

        mActiveSearchExpirationHandler = new Handler();

        mTeleportClient = new TeleportClient(this);
        mTeleportClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        EventManager.handleEvent(TAG, "onStartCommand", Log.DEBUG);

        mIsPassive = intent.getBooleanExtra(Properties.Keys.PASSIVE, true);
        mSendToWearable = intent.getBooleanExtra(Properties.Keys.SEND_TO_WEARABLE, false);
        boolean updateLocationRequest = intent.getBooleanExtra(Properties.Keys.UPDATE_LOCATION_REQUEST, true);

        mLocationRequest = LocationRequest.create();

        if (updateLocationRequest) {
            mActiveSearchExpirationHandler.removeCallbacks(mExpirationRunnable);
        }

        prepareLocationRequest();

        if (mLocationClient == null || !mLocationClient.isConnected()) {
            EventManager.handleEvent(TAG, "mLocationClient == null || !mLocationClient.isConnected()", Log.DEBUG);

            mLocationClient = new LocationClient(this, this, this);
            mLocationClient.connect();

        } else {
            if (updateLocationRequest) {
                trackAndLog("updateLocationRequest");

                mLocationClient.removeLocationUpdates(this);
                mLocationClient.requestLocationUpdates(mLocationRequest, this);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void prepareLocationRequest() {

        int priority;
        long interval;

        if (mIsPassive) {
            trackAndLog("passive locations request");
            int passiveIntervalMillis = getUpdateIntervalMillis();

            priority = LocationRequest.PRIORITY_NO_POWER;
            interval = passiveIntervalMillis;

            float displacement = Float.parseFloat(mSharedPreferences.getString(MINIMUM_LOCATION_DISTANCE, "35"));
            EventManager.handleEvent(TAG, "displacement: " + displacement, Log.VERBOSE);
            mLocationRequest.setSmallestDisplacement(displacement);

        } else {
            trackAndLog("active locations request");
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
            mLocationRequest.setExpirationDuration(ACTIVE_EXPIRATION_DURATION);

            interval = ACTIVE_EXPIRATION_DURATION;
            mLocationRequest.setNumUpdates(1);
            mActiveSearchExpirationHandler.postDelayed(mExpirationRunnable, ACTIVE_EXPIRATION_DURATION);
        }

        mLocationRequest.setPriority(priority);
        EventManager.handleEvent(TAG, "request interval: " + interval, Log.VERBOSE);
        mLocationRequest.setInterval(interval);
    }

    @Override
    public void onLocationChanged(Location location) {
        long nowMillis = System.currentTimeMillis();

        trackAndLog("LocationChanged from: " + location.getProvider());

        if (mLastLocation != null) {
            EventManager.handleEvent(TAG, "distance: " + mLastLocation.distanceTo(location) + "m", Log.VERBOSE);
        }
        mLastLocation = location;

        long lastLocationChangedTime = mSharedPreferences.getLong(Properties.SharePreferencesKeys.LOCATION_CHANGED_TIME, 0);
        if (mIsPassive && nowMillis - lastLocationChangedTime < getUpdateIntervalMillis()) {
            trackAndLog("not in persistent interval period");
            return;
        }

        mSharedPreferences.edit().putLong(Properties.SharePreferencesKeys.LOCATION_CHANGED_TIME, nowMillis).commit();

        SearchRequest request = new SearchRequest(location.getLatitude(), location.getLongitude());
        request.setRetryPolicy(new DefaultRetryPolicy());

        getSpiceManager().execute(request, this);
    }

    private int getUpdateIntervalMillis() {
        int updateIntervalSeconds =
                Integer.parseInt(mSharedPreferences.getString(PASSIVE_LOCATION_UPDATE_INTERVAL, "15"));
        return updateIntervalSeconds * 60 * MILLIS;
    }

    @Override
    public void onRequestSuccess(SearchResponse searchResponse) {
        onRequest(Properties.SUCCESS.NAME, Properties.SUCCESS.VALUE);

        ArrayList<Venue> venues = searchResponse.getResponse().getVenues();
        if (venues != null) {
            BusProvider.getInstance().post(new VenuesAvailableEvent(venues));
        }

        if (mSendToWearable) {
            sendToWearable(searchResponse);
        } else {
            updateNotifications(searchResponse);
        }
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        onRequest(Properties.FAILURE.NAME, Properties.FAILURE.VALUE);
        onSearchFinished();
    }

    private void onRequest(String name, long value) {
        EventManager.trackAndLogEvent(TAG, "search venues request", name, value);
    }

    private void updateNotifications(SearchResponse searchResponse) {
        getNotificationManager().cancelAll();
        trackAndLog("postNotifications");
        postNotifications(searchResponse);
    }

    private void postNotifications(SearchResponse searchResponse) {
        List<Venue> venues = searchResponse.getResponse().getVenues();

        if (venues == null || venues.isEmpty()) {
            trackAndLog("venues.isEmpty()");
            return;
        }

        int priority = mIsPassive ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_DEFAULT;

        for (int i = venues.size() - 1; i >= 0; i--) {
            Venue venue = venues.get(i);
            int id = i + 1;
            showVenueNotification(id, venue, priority);
        }

        boolean summaryNotificationEnabled =
                mSharedPreferences.getBoolean(Properties.PreferenceKeys.SUMMARY_NOTIFICATION, false);
        if (summaryNotificationEnabled) {
            showSummaryNotification(venues, priority);
        }

    }

    private void showSummaryNotification(List<Venue> allVenues, int priority) {

        // Group notification that will be visible on the phone
        Intent intent = new Intent(this, IntroductionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String[] nameArray = new String[allVenues.size()];
        String[] addressArray = new String[allVenues.size()];
        String[] venueIdArray = new String[allVenues.size()];

        String title = getString(R.string.app_name);
        String contentText = getString(R.string.places_checking_available);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText(contentText);

        for (int i = 0; i < allVenues.size(); i++) {
            Venue venue = allVenues.get(i);
            String name = venue.getName();

            nameArray[i] = name;
            addressArray[i] = venue.getLocation().getAddress();
            venueIdArray[i] = venue.getId();

            style.addLine(name);

        }

        Bundle bundle = new Bundle();
        Venue.fillBundle(bundle, nameArray, addressArray, venueIdArray);
        intent.replaceExtras(bundle);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_stat_bee)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_large))
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setGroupSummary(true)
                .setStyle(style)
                .setPriority(priority)
                .setGroup(VENUES_NOTIFICATION_GROUP);
        int notificationId = ((Long) System.currentTimeMillis()).hashCode();

        notify(builder.build(), notificationId);
    }

    private void showVenueNotification(int notificationId, Venue venue, int priority) {
        Intent intent = DoCheckinService.getServiceIntent(this, venue.getId());
        PendingIntent checkinPendingIntent = PendingIntent.getService(this, notificationId, intent, FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(venue.getName())
                .setContentText(venue.getLocation().getAddress())
                .setSmallIcon(R.drawable.ic_stat_bee)
                .extend(new NotificationCompat.WearableExtender()
                                .addAction(new NotificationCompat.Action.Builder(
                                        R.drawable.ic_action_checkin,
                                        getString(R.string.checkin),
                                        checkinPendingIntent)
                                        .build()).addAction(Utils.getSearchAction(this, notificationId))
                )
                .setPriority(priority)
                .setGroup(VENUES_NOTIFICATION_GROUP);

        notify(builder.build(), ((Long) System.currentTimeMillis()).hashCode());
    }

    private void notify(Notification summaryNotification, int notificationId) {
        getNotificationManager().notify(notificationId, summaryNotification);
    }

    @Override
    public void onConnected(Bundle bundle) {
        trackAndLog("Connected");
        if (mLocationClient.isConnected()) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }

    @Override
    public void onDisconnected() {
        trackAndLog("Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        String errorMessage = getString(R.string.google_play_services_connection_failed_with_resolution, errorCode);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void trackAndLog(String action) {
        EventManager.trackAndLogEvent(TAG, action);
    }

    private void sendToWearable(SearchResponse searchResponse) {
        ArrayList<Venue> venuesList = searchResponse.getResponse().getVenues();

        final ArrayList<DataMap> dataVenues = new ArrayList<>();
        for (Venue venue : venuesList) {
            dataVenues.add(venue.getDataMap());
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.createWithAutoAppendedId(Properties.Path.VENUES);
        dataMapRequest.getDataMap().putDataMapArrayList("venues", dataVenues);
        mTeleportClient.syncDataItem(dataMapRequest);
        handleImages(venuesList);

        // if images aren't downloaded in 5 secs, allow to destroy this service
        // TODO handle this based on (un)finished image requests
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onSearchFinished();
            }
        }, IMAGES_DOWNLOAD_TIMEOUT);
    }

    protected void handleImages(List<Venue> venueList) {
        for (Venue venue : venueList) {
            String primaryCategoryPNGIconUrl = venue.getPrimaryCategoryPNGIconUrl();

            if (TextUtils.isEmpty(primaryCategoryPNGIconUrl)) {
                continue;
            }

            File cacheFile = new File(getCacheDir(), "bitmapCache");
            EventManager.trackAndLogEvent(TAG, primaryCategoryPNGIconUrl);
            BitmapRequest bitmapRequest = new BitmapRequest(primaryCategoryPNGIconUrl, cacheFile);
            RequestListener<Bitmap> requestListener = new ImageRequestListener(venue);
            getSpiceManager().execute(bitmapRequest, requestListener);
        }
    }

    private class ImageRequestListener implements RequestListener<Bitmap> {

        private final Venue mVenue;

        public ImageRequestListener(Venue venue) {
            mVenue = venue;
        }

        @Override
        public void onRequestSuccess(final Bitmap bitmap) {
            Asset asset = createAssetFromBitmap(bitmap);
            String id = mVenue.getId();
            syncImageAsset(id, asset);
        }

        private Asset createAssetFromBitmap(Bitmap bitmap) {
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            String msg = String.format("image download for venue %s failed", mVenue.getId());
            EventManager.handleEvent(TAG, msg, Log.DEBUG);
        }

        public void syncImageAsset(String id, Asset asset) {
            final PutDataMapRequest data = PutDataMapRequest.createWithAutoAppendedId(Properties.Path.IMAGES);
            data.getDataMap().putString(Venue.ID, id);
            data.getDataMap().putAsset("asset", asset);
            mTeleportClient.syncDataItem(data);
        }
    }

    private void onSearchFinished() {
        boolean automaticNotifications = mSharedPreferences.getBoolean(AUTOMATIC_NOTIFICATIONS, false);

        if (automaticNotifications) {
            start(this, true, true); // start in passive mode
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        trackAndLog("onDestroy");

        mLocationClient.disconnect();
        mHandler.removeCallbacksAndMessages(null);
        mActiveSearchExpirationHandler.removeCallbacks(mExpirationRunnable);
        mTeleportClient.disconnect();

        super.onDestroy();
    }

}
