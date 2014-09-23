package io.seal.swarmwear.activity;

import android.os.Bundle;
import io.seal.swarmwear.R;
import io.seal.swarmwear.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.containerLayout, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }

}
