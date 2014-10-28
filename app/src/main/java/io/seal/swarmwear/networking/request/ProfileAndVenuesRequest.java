package io.seal.swarmwear.networking.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import io.seal.swarmwear.networking.response.ProfileSearchVenuesResponse;
import io.seal.swarmwear.networking.Foursquare;

public class ProfileAndVenuesRequest extends RetrofitSpiceRequest<ProfileSearchVenuesResponse, Foursquare.Api> {

    private final String mAccessToken;
    private double mLatitude, mLongitude;

    public ProfileAndVenuesRequest(String accessToken, double latitude, double longitude) {
        super(ProfileSearchVenuesResponse.class, Foursquare.Api.class);
        mAccessToken = accessToken;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Override
    public ProfileSearchVenuesResponse loadDataFromNetwork() {
        // TODO limit seems not working, it receives 30 venues instead of 12
        return getService().profileAndVenues(mAccessToken,
                Foursquare.VERSION, String.format("%s,%s", mLatitude, mLongitude), Foursquare.INTENT_CHECKIN, 12);
    }

}