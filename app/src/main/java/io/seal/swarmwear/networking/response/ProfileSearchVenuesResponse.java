package io.seal.swarmwear.networking.response;

@SuppressWarnings("UnusedDeclaration")
public class ProfileSearchVenuesResponse {

    private Response response;

    public Response getResponse() {
        return response;
    }

    public static class Response {

        private ProfileSearchResponse responses;

        public ProfileSearchResponse getResponses() {
            return responses;
        }
    }

}
