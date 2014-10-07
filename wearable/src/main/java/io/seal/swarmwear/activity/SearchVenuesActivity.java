package io.seal.swarmwear.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportClient;
import io.seal.swarmwear.R;
import io.seal.swarmwear.VenuesAdapter;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;

import java.util.ArrayList;
import java.util.List;

public class SearchVenuesActivity extends BaseTeleportActivity implements WearableListView.ClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SearchVenuesActivity";

    private ViewSwitcher mViewSwitcher;
    private WearableListView mWearableListView;
    private VenuesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO add logged in check

        setContentView(R.layout.activity_search_venues);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        mWearableListView = (WearableListView) findViewById(R.id.wearableList);
        changeBackgroundColor(R.color.yellow);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleApiClient googleApiClient = getTeleportClient().getGoogleApiClient();
        googleApiClient.registerConnectionCallbacks(this);
        googleApiClient.registerConnectionFailedListener(this);

        getTeleportClient().setOnSyncDataItemTaskBuilder(null);
        getTeleportClient().setOnSyncDataItemTask(new OnVenuesSyncDataItemTask());
        getTeleportClient().sendMessage(Properties.Path.SEARCH_VENUES, null);
    }

    @Override
    protected void onStop() {
        GoogleApiClient googleApiClient = getTeleportClient().getGoogleApiClient();
        googleApiClient.unregisterConnectionCallbacks(this);
        googleApiClient.unregisterConnectionFailedListener(this);
        super.onStop();
    }

    private class OnVenuesSyncDataItemTask extends TeleportClient.OnSyncDataItemTask {
        @Override
        protected void onPostExecute(DataMap result) {
            if (result.containsKey("venues")) {

                Log.d(TAG, "venues received");
                onVenuesReceived(result);
                getTeleportClient().setOnSyncDataItemTaskBuilder(new TeleportClient.OnSyncDataItemTask.Builder() {
                    @Override
                    public TeleportClient.OnSyncDataItemTask build() {
                        return new OnImageSyncDataItemTask();
                    }
                });

            } else {
                logUnknownResult();
            }
        }
    }

    private class OnImageSyncDataItemTask extends TeleportClient.OnSyncDataItemTask {
        @Override
        protected void onPostExecute(DataMap result) {
            if (result.containsKey("asset")) {
                Log.d(TAG, "image received");
                onImageReceived(result);
            } else {
                logUnknownResult();
            }
        }
    }

    private void logUnknownResult() {
        Log.w(TAG, "unknown result");
    }

    private void onVenuesReceived(DataMap result) {
        List<DataMap> dataMapList = result.getDataMapArrayList("venues");
        List<Venue> venuesList = new ArrayList<>(dataMapList.size());

        for (DataMap dataMap : dataMapList) {
            Venue venue = Venue.extractFromDataMap(dataMap);
            venuesList.add(venue);
        }

        if (mAdapter == null) {
            mAdapter = new VenuesAdapter(venuesList);
            mWearableListView.setAdapter(mAdapter);
            mWearableListView.setClickListener(this);
            changeBackgroundColor(R.color.orange_normal);
            mViewSwitcher.showNext();
        } else if (mAdapter.updateVenues(venuesList)) {
            mAdapter.notifyDataSetChanged();
        }

    }

    private void changeBackgroundColor(int orange_normal) {
        getWindow().setBackgroundDrawableResource(orange_normal);
    }

    private void onImageReceived(DataMap result) {
        String id = result.getString(Venue.ID);

        for (final Venue venue : mAdapter.getVenuesList()) {

            if (venue.getPrimaryCategoryBitmap() != null || !venue.getId().equals(id)) {
                continue;
            }

            Asset asset = result.getAsset("asset");
            new TeleportClient.ImageFromAssetTask() {
                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    venue.setPrimaryCategoryBitmap(bitmap);
                    mAdapter.notifyDataSetChanged();
                }
            }.execute(asset, getTeleportClient().getGoogleApiClient());
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        String id = (String) viewHolder.itemView.getTag();
        String name = ((TextView) viewHolder.itemView.findViewById(R.id.txtName)).getText().toString();

        Intent intent = new Intent(this, CheckInDelayedConfirmationActivity.class);
        intent.putExtra(Venue.ID, id);
        intent.putExtra(Venue.NAME, name);

        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Google Play Services connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended");
        showErrorAndFinish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed");
        showErrorAndFinish();
    }

    private void showErrorAndFinish() {
        Toast.makeText(SearchVenuesActivity.this, R.string.sorry_error, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

}