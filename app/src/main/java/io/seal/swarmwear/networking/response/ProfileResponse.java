package io.seal.swarmwear.networking.response;

import android.text.TextUtils;

@SuppressWarnings("UnusedDeclaration")
public class ProfileResponse {

    private Response response;

    public static class Response {

        private User user;

        public static class User {

            private Contact contact;

            public static class Contact {

                private String facebook;
                private String twitter;

                public boolean hasFacebook() {
                    return !TextUtils.isEmpty(facebook);
                }

                public boolean hasTwitter() {
                    return !TextUtils.isEmpty(twitter);
                }

            }

            public Contact getContact() {
                return contact;
            }
        }

        public User getUser() {
            return user;
        }
    }

    public Response getResponse() {
        return this.response;
    }

}
