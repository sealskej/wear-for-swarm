package io.seal.swarmwear.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.service.SearchVenuesService;

import static io.seal.swarmwear.lib.Properties.PreferenceKeys.AUTOMATIC_NOTIFICATIONS;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.MINIMUM_LOCATION_DISTANCE;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.PASSIVE_LOCATION_UPDATE_INTERVAL;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.SUMMARY_NOTIFICATION;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Preference automaticNotificationsPreference = findPreference(AUTOMATIC_NOTIFICATIONS);
        Preference summaryNotificationPreference = findPreference(SUMMARY_NOTIFICATION);
        Preference intervalPreference = findPreference(PASSIVE_LOCATION_UPDATE_INTERVAL);
        Preference distancePreference = findPreference(MINIMUM_LOCATION_DISTANCE);

        intervalPreference.setSummary(sharedPreferences.getString(PASSIVE_LOCATION_UPDATE_INTERVAL, "15"));
        distancePreference.setSummary(sharedPreferences.getString(MINIMUM_LOCATION_DISTANCE, "35"));

        automaticNotificationsPreference.setOnPreferenceChangeListener(this);
        summaryNotificationPreference.setOnPreferenceChangeListener(this);
        intervalPreference.setOnPreferenceChangeListener(this);
        distancePreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventManager.trackScreenView(TAG);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        String key = preference.getKey();

        Context applicationContext = getActivity().getApplicationContext();

        Object objectValue;
        if (newValue instanceof Boolean) {
            objectValue = Boolean.parseBoolean(newValue + "") ? 1 : 0;
        } else {
            objectValue = newValue;
        }

        EventManager.trackAndLogEvent(TAG, "preference_changed", key, Long.valueOf(objectValue + ""));

        //noinspection ConstantConditions
        switch (key) {
            case AUTOMATIC_NOTIFICATIONS:
                if (newValue == Boolean.TRUE) {
                    SearchVenuesService.start(getActivity(), true, true);
                    EventManager.trackAndLogEvent(TAG, "SearchVenuesService started");
                } else {
                    getActivity().stopService(new Intent(getActivity(), SearchVenuesService.class));
                    EventManager.trackAndLogEvent(TAG, "SearchVenuesService stopped");
                }
                break;
            case SUMMARY_NOTIFICATION:
                return true;
            default:
                if (newValue instanceof String) {
                    preference.setSummary((String) newValue);
                    SearchVenuesService.start(getActivity(), true, true);
                }
                break;
        }

        return true;
    }
}
