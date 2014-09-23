package io.seal.swarmwear.networking;

import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;
import io.seal.swarmwear.BuildConfig;
import retrofit.RestAdapter;

public class FoursquareRetrofitSpiceService extends RetrofitGsonSpiceService {

    private final static String BASE_URL = "https://api.foursquare.com";

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(Foursquare.Api.class);
    }

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {
        RestAdapter.Builder adapter = super.createRestAdapterBuilder();
        if (BuildConfig.DEBUG) {
            adapter.setLogLevel(RestAdapter.LogLevel.FULL);
        }
        return adapter;
    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }

}