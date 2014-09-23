package io.seal.swarmwear.activity;

import android.app.Activity;
import android.os.Bundle;
import com.mariux.teleport.lib.TeleportClient;

public abstract class BaseTeleportActivity extends Activity {

    private TeleportClient mTeleportClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTeleportClient = new TeleportClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
    }

    @Override
    protected void onStop() {
        mTeleportClient.disconnect();
        super.onStop();
    }

    protected TeleportClient getTeleportClient() {
        return mTeleportClient;
    }
}
