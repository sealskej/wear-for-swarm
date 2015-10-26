package io.seal.swarmwear.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import io.seal.swarmwear.event.VenuesAvailableEvent;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;
import io.seal.swarmwear.model.search.SearchResponse;
import io.seal.swarmwear.networking.request.SearchRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchVenuesService extends BaseSpiceManagerService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener, RequestListener<SearchResponse> {

    private static final String TAG = "SearchVenuesService";

    private static final long ACTIVE_EXPIRATION_DURATION = TimeUnit.SECONDS.toMillis(30);
    private static final long IMAGES_DOWNLOAD_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    private LocationClient mLocationClient;
    private Handler mHandler = new Handler();
    private TeleportClient mTeleportClient;

    private Runnable mExpirationRunnable = new Runnable() {
        @Override
        public void run() {
            onSearchFinished();
        }
    };

    public static void start(Context context) {
        EventManager.trackAndLogEvent(TAG, "start");
        Context appContext = context.getApplicationContext();
        Intent intent = new Intent(appContext, SearchVenuesService.class);
        appContext.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.trackAndLogEvent(TAG, "onCreate");

        mTeleportClient = new TeleportClient(this);
        mTeleportClient.connect();

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EventManager.handleEvent(TAG, "onStartCommand", Log.DEBUG);

        mHandler.postDelayed(mExpirationRunnable, ACTIVE_EXPIRATION_DURATION);
        mHandler.removeCallbacksAndMessages(null);

        if (mLocationClient.isConnected()) {
            requestLocationUpdates();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        SearchRequest request = new SearchRequest(location.getLatitude(), location.getLongitude());
        request.setRetryPolicy(new DefaultRetryPolicy());
        getSpiceManager().execute(request, this);
    }

    @Override
    public void onRequestSuccess(SearchResponse searchResponse) {
        onRequest(Properties.SUCCESS.NAME, Properties.SUCCESS.VALUE);

        ArrayList<Venue> venues = searchResponse.getResponse().getVenues();
        if (venues != null) {
            BusProvider.getInstance().post(new VenuesAvailableEvent(venues));
        }

        sendToWearable(searchResponse);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        onRequest(Properties.FAILURE.NAME, Properties.FAILURE.VALUE);
        onSearchFinished();
    }

    private void onRequest(String name, long value) {
        EventManager.trackAndLogEvent(TAG, "search venues request", name, value);
    }


    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setNumUpdates(1);
        locationRequest.setExpirationDuration(ACTIVE_EXPIRATION_DURATION);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(ACTIVE_EXPIRATION_DURATION);

        mLocationClient.requestLocationUpdates(locationRequest, this);
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
        stopSelf();
    }

    @Override
    public void onDestroy() {
        trackAndLog("onDestroy");

        mLocationClient.disconnect();
        mHandler.removeCallbacksAndMessages(null);
        mTeleportClient.disconnect();

        super.onDestroy();
    }

}
