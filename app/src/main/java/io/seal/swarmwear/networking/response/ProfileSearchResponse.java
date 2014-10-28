package io.seal.swarmwear.networking.response;

public class ProfileSearchResponse {

    private ProfileResponse mProfileResponse;
    private SearchResponse mSearchResponse;

    public ProfileSearchResponse(ProfileResponse profileResponse, SearchResponse searchResponse) {
        mProfileResponse = profileResponse;
        mSearchResponse = searchResponse;
    }

    public ProfileResponse getProfileResponse() {
        return mProfileResponse;
    }

    public SearchResponse getSearchResponse() {
        return mSearchResponse;
    }

}
