package io.seal.swarmwear.networking;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import io.seal.swarmwear.BuildConfig;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.model.docheckin.DoCheckinResponse;
import io.seal.swarmwear.model.search.SearchResponse;
import org.jetbrains.annotations.NotNull;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public class Foursquare {

    public static final String UNIVERSAL_HOST = String.format("foursquare.com/oauth2/authenticate" +
            "?client_id=%s&response_type=token"
            + "&redirect_uri=null", BuildConfig.FOURSQUARE_CLIENT_ID);
    public static String AUTH_URL = String.format("https://" + UNIVERSAL_HOST);
    public static int VERSION = 20140610;
    public static String INTENT_CHECKIN = "checkin";

    public static void saveAccessToken(@NotNull Context context, @NotNull String accessToken) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Properties.SharePreferencesKeys.FOURSQUARE_ACCESS_TOKEN, accessToken);
        editor.commit();
    }

    public static String getAccessToken(@NotNull Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPref.getString(Properties.SharePreferencesKeys.FOURSQUARE_ACCESS_TOKEN, null);
    }

    public static boolean isLoggedIn(@NotNull Context context) {
        return !TextUtils.isEmpty(getAccessToken(context.getApplicationContext()));
    }

    public static interface Api {

        @GET("/v2/venues/search")
        SearchResponse search(@Query("client_id") String clientId,
                              @Query("client_secret") String clientSecret,
                              @Query("v") int version,
                              @Query("ll") String location,
                              @Query("intent") String intent,
                              @Query("limit") int limit);

        @FormUrlEncoded
        @POST("/v2/checkins/add")
        DoCheckinResponse doCheckin(@Field("client_id") String clientId,
                                    @Field("client_secret") String clientSecret,
                                    @Field("v") int version,
                                    @Field("oauth_token") String oAuthToken,
                                    @Field("venueId") String venueId);

    }
}
