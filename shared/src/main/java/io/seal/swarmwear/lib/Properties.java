package io.seal.swarmwear.lib;

public class Properties {

    public static class SUCCESS {
        public static String NAME = "success";
        public static long VALUE = 0;
    }

    public static class FAILURE {
        public static String NAME = "failure";
        public static long VALUE = 1;
    }

    public static class SharePreferencesKeys {
        public static final String FOURSQUARE_ACCESS_TOKEN = "foursquare_access_token";
        public static final String LOCATION_CHANGED_TIME = "location_changed_time";
    }

    public static class Keys {
        public static final String PASSIVE = "passive";
        public static final String UPDATE_LOCATION_REQUEST = "update_location_request";
        public static final String VENUE_ID = "venue_id";
        public static final String VENUE_NAMES_ARRAY = "venue_name_array";
        public static final String VENUE_ADDRESS_ARRAY = "venue_address_array";
        public static final String VENUE_ID_ARRAY = "venue_id_array";
        public static final String GOOGLE_PLAY_CONNECTION_RESULT = "google_play_connection_result";
        public static final String SEND_TO_WEARABLE = "send_to_wearable";
    }

    public static class PreferenceKeys {
        public static final String AUTOMATIC_NOTIFICATIONS = "automatic_notifications";
        public static final String SUMMARY_NOTIFICATION = "summary_notification";
        public static final String PASSIVE_LOCATION_UPDATE_INTERVAL = "passive_location_update_interval";
        public static final String MINIMUM_LOCATION_DISTANCE = "minimum_location_distance";
        public static final String FACEBOOK_SHARE = "facebook_share";
        public static final String TWITTER_SHARE = "twitter_share";
    }

    public static class Path {
        public static final String VENUES = "/venues";
        public static final String SEARCH_VENUES = VENUES + "/search";
        public static final String CHECK_IN = VENUES + "/checkin";
        public static final String IMAGES = VENUES + "/images";
    }

}
