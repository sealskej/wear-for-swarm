package io.seal.swarmwear;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BaseListFragment extends ListFragment {

    private static final String TAG = "BaseFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logDebug("onCreate");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logDebug("onViewCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        logDebug("onDestroy");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logDebug("onDestroy");
    }

    private void logDebug(String action) {
        EventManager.handleEvent(TAG, action, Log.DEBUG);
    }

}
