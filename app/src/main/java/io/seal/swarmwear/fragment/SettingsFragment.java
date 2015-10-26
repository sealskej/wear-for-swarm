package io.seal.swarmwear.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventManager.trackScreenView(TAG);
    }

}
