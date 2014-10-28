package io.seal.swarmwear.service;

import com.google.android.gms.wearable.DataMap;
import com.mariux.teleport.lib.TeleportService;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;

public class WearListenerService extends TeleportService {

    private static final String TAG = "WearListenerService";

    @Override
    public void onCreate() {
        super.onCreate();

        setOnGetMessageTaskBuilder(new OnGetMessageTask.Builder() {
            @Override
            public OnGetMessageTask build() {
                return new VenuesOnGetMessageTask();
            }
        });

        setOnSyncDataItemTaskBuilder(new CheckinOnSyncDataItemTask());
    }

    private class VenuesOnGetMessageTask extends TeleportService.OnGetMessageTask {
        @Override
        protected void onPostExecute(String path) {
            if (path.startsWith(Properties.Path.SEARCH_VENUES)) {
                SearchVenuesService.start(WearListenerService.this, false, true, true);
            } else {
                EventManager.trackAndLogEvent(TAG, "unknown path");
            }
        }
    }

    private class CheckinOnSyncDataItemTask extends OnSyncDataItemTask.Builder {
        @Override
        public OnSyncDataItemTask build() {
            return new OnSyncDataItemTask() {
                @Override
                protected void onPostExecute(DataMap result) {
                    if (result.getBoolean(Properties.CHECKIN)) {
                        String id = result.getString(Venue.ID);
                        int socialNetworks = result.getInt(Properties.SOCIAL_NETWORKS);

                        DoCheckinService.start(WearListenerService.this, id, socialNetworks);
                    }
                }
            };
        }
    }

}
