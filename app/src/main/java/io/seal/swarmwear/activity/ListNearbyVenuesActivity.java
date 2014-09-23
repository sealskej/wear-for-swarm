package io.seal.swarmwear.activity;

import android.os.Bundle;
import io.seal.swarmwear.R;
import io.seal.swarmwear.fragment.ListNearbyVenuesFragment;

public class ListNearbyVenuesActivity extends BaseActivity {
    private static final String TAG = "ListNearbyVenuesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_container);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.containerLayout, new ListNearbyVenuesFragment()).commit();
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
