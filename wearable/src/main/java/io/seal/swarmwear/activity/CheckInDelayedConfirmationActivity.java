package io.seal.swarmwear.activity;

import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import io.seal.swarmwear.CountDownView;
import io.seal.swarmwear.R;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;

import java.util.concurrent.TimeUnit;

public class CheckInDelayedConfirmationActivity extends BaseTeleportActivity
        implements DelayedConfirmationView.DelayedConfirmationListener {

    private static final String TAG = "CheckInDelayedConfirmationActivity";

    public static final int CONFIRMATION_TIMEOUT_SECONDS = 5;

    private String mId;
    private CheckBox mSocialCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delayed_confirmation);

        final DelayedConfirmationView cancelConfirmationView = (DelayedConfirmationView) findViewById(R.id.confirmationCancel);
        cancelConfirmationView.setProgress(0);
        showLater(cancelConfirmationView);

        TextView nameTextView = (TextView) findViewById(R.id.txtName);
        CountDownView countDownView = (CountDownView) findViewById(R.id.countDown);
        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        mSocialCheckBox = (CheckBox) findViewById(R.id.checkBoxSocial);

        mId = getIntent().getStringExtra(Venue.ID);
        String name = getIntent().getStringExtra(Venue.NAME);

        nameTextView.setText(name);

        cancelConfirmationView.setTotalTimeMs(TimeUnit.SECONDS.toMillis(CONFIRMATION_TIMEOUT_SECONDS));
        cancelConfirmationView.start();
        cancelConfirmationView.setListener(this);
        cancelConfirmationView.setTag(R.id.key_cancel, Boolean.FALSE);

        countDownView.start(CONFIRMATION_TIMEOUT_SECONDS);

        int socialNetworks = getIntent().getIntExtra(Properties.SOCIAL_NETWORKS, 0);

        if (socialNetworks > 0) {
            viewSwitcher.showNext();
        }
    }

    /**
     * Show DelayedConfirmationView few milliseconds later, because on Moto 360 there  is full progress
     * shown, although progress 0 was set before
     */
    private void showLater(final View view) {
        view.setVisibility(View.INVISIBLE);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    @Override
    public void onTimerFinished(View view) {
        if (view.getTag(R.id.key_cancel) == Boolean.FALSE) {

            PutDataMapRequest dataMapRequest = PutDataMapRequest.createWithAutoAppendedId(Properties.Path.VENUES);
            DataMap dataMap = dataMapRequest.getDataMap();
            dataMap.putBoolean(Properties.CHECKIN, true);
            dataMap.putString(Venue.ID, mId);

            if (mSocialCheckBox.isChecked()) {
                int socialNetworks = getIntent().getIntExtra(Properties.SOCIAL_NETWORKS, 0);
                dataMap.putInt(Properties.SOCIAL_NETWORKS, socialNetworks);
            }

            getTeleportClient().syncDataItem(dataMapRequest);

            finishAffinity();
        } else {
            finish();
        }
    }

    @Override
    public void onTimerSelected(View view) {
        view.setTag(R.id.key_cancel, Boolean.TRUE);
        finish();
    }

}
