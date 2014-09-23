package io.seal.swarmwear.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.squareup.otto.Subscribe;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.BusProvider;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.Utils;
import io.seal.swarmwear.event.FoursquareAccessTokenAvailableEvent;
import io.seal.swarmwear.fragment.ConnectFragment;
import io.seal.swarmwear.fragment.WebLoginFragment;
import io.seal.swarmwear.lib.Properties;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_container);

        Intent connectIntent = FoursquareOAuth.getConnectIntent(this, BuildConfig.FOURSQUARE_CLIENT_ID);

        if (savedInstanceState == null) {
            Fragment fragment;
            if (Utils.isActivityAvailable(this, connectIntent) &&
                    !connectIntent.getData().toString().contains("market://")) {
                fragment = new ConnectFragment();
                trackFoursquareInstalled(Properties.SUCCESS.NAME, Properties.SUCCESS.VALUE);
            } else {
                EventManager.trackAndLogEvent(TAG, "foursquare app not installed, starting internal foursquare login");
                fragment = new WebLoginFragment();
                trackFoursquareInstalled(Properties.FAILURE.NAME, Properties.FAILURE.VALUE);
            }
            getFragmentManager().beginTransaction().add(R.id.containerLayout, fragment).commit();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @Subscribe
    public void foursquareAccessTokenAvailable(FoursquareAccessTokenAvailableEvent event) {
        startActivity(new Intent(this, IntroductionActivity.class));
        finish();
    }

    private void trackFoursquareInstalled(String label, long value) {
        EventManager.trackAndLogEvent(TAG, "foursquare installed", label, value);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onStop() {
        BusProvider.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
