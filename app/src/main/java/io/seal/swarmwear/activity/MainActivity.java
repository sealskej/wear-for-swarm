package io.seal.swarmwear.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import io.seal.swarmwear.networking.Foursquare;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO check in Android Wear Preview app is installed?
        // TODO check if bluetooth enabled?
        // TODO check if Android Wear Preview is enabled in Notifications Access?
        // TODO check if location services are enabled?

        Class<? extends Activity> clazz;
        if (Foursquare.isLoggedIn(this)) {
            clazz = IntroductionActivity.class;
        } else {
            clazz = LoginActivity.class;
        }

        startActivity(new Intent(this, clazz));
    }

}
