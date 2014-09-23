package io.seal.swarmwear.networking.request;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.model.search.SearchResponse;
import io.seal.swarmwear.networking.Foursquare;

public class SearchRequest extends RetrofitSpiceRequest<SearchResponse, Foursquare.Api> {

    private double mLatitude, mLongitude;

    public SearchRequest(double latitude, double longitude) {
        super(SearchResponse.class, Foursquare.Api.class);

        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Override
    public SearchResponse loadDataFromNetwork() {
        return getService().search(BuildConfig.FOURSQUARE_CLIENT_ID, BuildConfig.FOURSQUARE_CLIENT_SECRET,
                Foursquare.VERSION, String.format("%s,%s", mLatitude, mLongitude), Foursquare.INTENT_CHECKIN, 12);
    }
}