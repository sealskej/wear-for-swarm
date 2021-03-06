package io.seal.swarmwear.activity;

import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;
import android.widget.TextView;
import io.seal.swarmwear.CountDownView;
import io.seal.swarmwear.R;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;

import java.util.concurrent.TimeUnit;

public class CheckInDelayedConfirmationActivity extends BaseTeleportActivity
        implements DelayedConfirmationView.DelayedConfirmationListener {

    public static final int CONFIRMATION_TIMEOUT_SECONDS = 5;

    private String mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delayed_confirmation);

        final DelayedConfirmationView cancelConfirmationView = (DelayedConfirmationView) findViewById(R.id.confirmationCancel);
        cancelConfirmationView.setProgress(0);
        showLater(cancelConfirmationView);

        TextView nameTextView = (TextView) findViewById(R.id.txtName);
        CountDownView countDownView = (CountDownView) findViewById(R.id.countDown);

        mId = getIntent().getStringExtra(Venue.ID);
        String name = getIntent().getStringExtra(Venue.NAME);

        nameTextView.setText(name);

        cancelConfirmationView.setTotalTimeMs(TimeUnit.SECONDS.toMillis(CONFIRMATION_TIMEOUT_SECONDS));
        cancelConfirmationView.start();
        cancelConfirmationView.setListener(this);
        cancelConfirmationView.setTag(R.id.key_cancel, Boolean.FALSE);

        countDownView.start(CONFIRMATION_TIMEOUT_SECONDS);
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
            getTeleportClient().sendMessage(Properties.Path.CHECK_IN + '/' + mId, null);
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
