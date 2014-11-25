package io.seal.swarmwear;

import android.app.Application;
import fr.nicolaspomepuy.androidwearcrashreport.wear.CrashReporter;

public class WearApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReporter.getInstance(this).start();
    }

}
