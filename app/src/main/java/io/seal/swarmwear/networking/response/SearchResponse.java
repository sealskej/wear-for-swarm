package io.seal.swarmwear.networking.response;

import io.seal.swarmwear.lib.model.Venue;

import java.util.ArrayList;

public class SearchResponse {

    @SuppressWarnings("unused")
    private Response response;

    public Response getResponse() {
        return this.response;
    }

    public static class Response {

        @SuppressWarnings("unused")
        private ArrayList<Venue> venues;

        public ArrayList<Venue> getVenues() {
            return this.venues;
        }

    }
}
