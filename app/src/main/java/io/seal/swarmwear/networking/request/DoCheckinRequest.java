package io.seal.swarmwear.networking.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.model.docheckin.DoCheckinResponse;
import io.seal.swarmwear.networking.Foursquare;

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
        return getService().doCheckin(BuildConfig.FOURSQUARE_CLIENT_ID, BuildConfig.FOURSQUARE_CLIENT_SECRET,
                Foursquare.VERSION, mAccessToken, mVenueId);
    }
}