package io.seal.swarmwear.networking.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.networking.Foursquare;
import io.seal.swarmwear.networking.response.DoCheckinResponse;

import static io.seal.swarmwear.lib.Properties.SOCIAL_NETWORK_CODE;

public class DoCheckinRequest extends RetrofitSpiceRequest<DoCheckinResponse, Foursquare.Api> {

    private final String mAccessToken;
    private final String mVenueId;
    private final int mSocialNetworks;

    public DoCheckinRequest(String accessToken, String venueId, int socialNetworks) {
        super(DoCheckinResponse.class, Foursquare.Api.class);
        mAccessToken = accessToken;
        mVenueId = venueId;
        mSocialNetworks = socialNetworks;
    }

    @Override
    public DoCheckinResponse loadDataFromNetwork() {

        StringBuilder broadcastBuilder = new StringBuilder();
        broadcastBuilder.append("public");

        if ((mSocialNetworks & SOCIAL_NETWORK_CODE.FACEBOOK) == SOCIAL_NETWORK_CODE.FACEBOOK) {
            broadcastBuilder.append(",facebook");
        }
        if ((mSocialNetworks & SOCIAL_NETWORK_CODE.TWITTER) == SOCIAL_NETWORK_CODE.TWITTER) {
            broadcastBuilder.append(",twitter");
        }

        return getService().doCheckin(BuildConfig.FOURSQUARE_CLIENT_ID, BuildConfig.FOURSQUARE_CLIENT_SECRET,
                Foursquare.VERSION, mAccessToken, mVenueId, broadcastBuilder.toString());
    }
}