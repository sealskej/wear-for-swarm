package io.seal.swarmwear.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import io.seal.swarmwear.EventManager;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logDebug("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventManager.trackScreenView(getTag());
    }

    @Override
    protected void onStart() {
        super.onStart();
        logDebug("onStart");
    }

    @Override
    protected void onStop() {
        logDebug("onStop");
        super.onStop();
    }

    protected abstract String getTag();

    private void logDebug(String action) {
        EventManager.handleEvent(getTag(), action, Log.DEBUG);
    }


}
