package io.seal.swarmwear.activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.Utils;
import io.seal.swarmwear.lib.Properties;

import static io.seal.swarmwear.lib.Properties.FAILURE;
import static io.seal.swarmwear.lib.Properties.SUCCESS;

public class IntroductionActivity extends BaseActivity {

    private static final String TAG = "IntroductionActivity";

    private static final int REQUEST_RESOLVE_ERROR = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        if (Utils.isPersistentLogEnabled()) {
            addMenuItem(menu, "Console", new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    startActivity(new Intent(IntroductionActivity.this, ConsoleActivity.class));
                    return true;
                }
            });
            addMenuItem(menu, "Venues List", new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    startActivity(new Intent(IntroductionActivity.this, ListNearbyVenuesActivity.class));
                    return true;
                }
            });
        }

        return true;
    }

    private void addMenuItem(Menu menu, String title, MenuItem.OnMenuItemClickListener menuItemClickListener) {
        MenuItem menuItem = menu.add(title);
        menuItem.setOnMenuItemClickListener(menuItemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        int googlePlayConnectionResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (googlePlayConnectionResult != ConnectionResult.SUCCESS) {
            GooglePlayServicesFragment.newInstance(googlePlayConnectionResult).show(getFragmentManager(), null);
            EventManager.trackAndLogEvent(TAG, "google play services available", FAILURE.NAME, FAILURE.VALUE);
        } else {
            EventManager.trackAndLogEvent(TAG, "google play services available", SUCCESS.NAME, SUCCESS.VALUE);
        }
    }

    public static class GooglePlayServicesFragment extends DialogFragment {

        public static DialogFragment newInstance(int googlePlayConnectionResult) {
            Bundle arguments = new Bundle();
            arguments.putInt(Properties.Keys.GOOGLE_PLAY_CONNECTION_RESULT, googlePlayConnectionResult);
            DialogFragment fragment = new GooglePlayServicesFragment();
            fragment.setArguments(arguments);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = getArguments().getInt(Properties.Keys.GOOGLE_PLAY_CONNECTION_RESULT);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), REQUEST_RESOLVE_ERROR);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
