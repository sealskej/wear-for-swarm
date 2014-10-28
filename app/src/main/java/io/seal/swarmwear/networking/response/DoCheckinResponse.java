package io.seal.swarmwear.networking.response;

@SuppressWarnings("UnusedDeclaration")
public class DoCheckinResponse {

    private Response response;

    public static class Response {

        private Checkin checkin;

        public Checkin getCheckin() {
            return this.checkin;
        }

        public static class Checkin {

            private String id;

            public String getId() {
                return this.id;
            }

        }
    }
}
