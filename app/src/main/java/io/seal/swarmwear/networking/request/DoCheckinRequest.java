package io.seal.swarmwear.networking.request;

import android.preference.PreferenceManager;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.PhoneApp;
import io.seal.swarmwear.model.docheckin.DoCheckinResponse;
import io.seal.swarmwear.networking.Foursquare;

import static io.seal.swarmwear.lib.Properties.PreferenceKeys.FACEBOOK_SHARE;
import static io.seal.swarmwear.lib.Properties.PreferenceKeys.TWITTER_SHARE;

public class DoCheckinRequest extends RetrofitSpiceRequest<DoCheckinResponse, Foursquare.Api> {

    private final String mAccessToken;
    private final String mVenueId;

    public DoCheckinRequest(String accessToken, String venueId) {
        super(DoCheckinResponse.class, Foursquare.Api.class);
        mAccessToken = accessToken;
        mVenueId = venueId;
    }

    @Override
    public DoCheckinResponse loadDataFromNetwork() {
        PhoneApp phoneApp = PhoneApp.getInstance();

        boolean facebookShare = PreferenceManager.getDefaultSharedPreferences(phoneApp).getBoolean(FACEBOOK_SHARE, false);
        boolean twitterShare = PreferenceManager.getDefaultSharedPreferences(phoneApp).getBoolean(TWITTER_SHARE, false);

        StringBuilder broadcastBuilder = new StringBuilder();
        broadcastBuilder.append("public");

        if (facebookShare) {
            broadcastBuilder.append(",facebook");
        }
        if (twitterShare) {
            broadcastBuilder.append(",twitter");
        }

        return getService().doCheckin(BuildConfig.FOURSQUARE_CLIENT_ID, BuildConfig.FOURSQUARE_CLIENT_SECRET,
                Foursquare.VERSION, mAccessToken, mVenueId, broadcastBuilder.toString());
    }
}