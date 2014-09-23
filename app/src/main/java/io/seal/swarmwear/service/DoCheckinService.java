package io.seal.swarmwear.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.Utils;
import io.seal.swarmwear.activity.IntroductionActivity;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.model.docheckin.DoCheckinResponse;
import io.seal.swarmwear.networking.Foursquare;
import io.seal.swarmwear.networking.request.DoCheckinRequest;

import static io.seal.swarmwear.lib.Properties.FAILURE;
import static io.seal.swarmwear.lib.Properties.Keys.VENUE_ID;
import static io.seal.swarmwear.lib.Properties.SUCCESS;

public class DoCheckinService extends BaseSpiceRequestService<DoCheckinResponse> {

    private static final String TAG = "DoCheckinService";

    public static void start(Context context, String venueId) {
        Intent checkinIntent = DoCheckinService.getServiceIntent(context, venueId);
        context.startService(checkinIntent);
    }

    protected static Intent getServiceIntent(Context context, String venueId) {
        Intent checkinIntent = new Intent(context.getApplicationContext(), DoCheckinService.class);
        checkinIntent.putExtra(Properties.Keys.VENUE_ID, venueId);
        return checkinIntent;
    }

    protected SpiceRequest<DoCheckinResponse> onCreateRequest(Intent intent) {
        String venueId = intent.getStringExtra(VENUE_ID);

        if (TextUtils.isEmpty(venueId)) {
            throw new IllegalArgumentException(String.format("intent [%s] must not be null!", VENUE_ID));
        }

        return new DoCheckinRequest(Foursquare.getAccessToken(this), venueId);
    }

    @Override
    public void onRequestSuccess(DoCheckinResponse doCheckinResponse) {
        // TODO add action to cancel checkin
        onRequest(R.string.checkin_successful, TAG, SUCCESS.NAME, SUCCESS.VALUE);
    }

    @Override
    public void onRequestFailure(SpiceException spiceException) {
        // TODO add action to try again
        onRequest(R.string.checkin_failed, TAG, FAILURE.NAME, FAILURE.VALUE);
    }

    private void onRequest(int contentTextResId, String category, String label, long value) {
        getNotificationManager().cancelAll();
        showNotification(contentTextResId);
        stopSelf();
        EventManager.trackAndLogEvent(category, "do checkin request result", label, value);
    }

    private void showNotification(int contentTextResId) {

        Intent intent = new Intent(this, IntroductionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(contentTextResId))
                .setSmallIcon(R.drawable.ic_stat_bee)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_large))
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                .setAutoCancel(true)
                .extend(
                        new NotificationCompat
                                .WearableExtender()
                                .addAction(Utils.getSearchAction(this, 0))
                );


        getNotificationManager().notify(((Long) System.currentTimeMillis()).hashCode(), notificationBuilder.build());

    }

}
