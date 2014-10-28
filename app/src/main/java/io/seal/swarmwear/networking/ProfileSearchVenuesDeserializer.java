package io.seal.swarmwear.networking;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.seal.swarmwear.networking.response.ProfileSearchResponse;
import io.seal.swarmwear.networking.response.ProfileResponse;
import io.seal.swarmwear.networking.response.SearchResponse;

import java.lang.reflect.Type;

public class ProfileSearchVenuesDeserializer implements JsonDeserializer<ProfileSearchResponse> {

    @Override
    public ProfileSearchResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        ProfileResponse profileResponse = context.deserialize(array.get(0), ProfileResponse.class);
        SearchResponse searchResponse = context.deserialize(array.get(1), SearchResponse.class);
        return new ProfileSearchResponse(profileResponse, searchResponse);
    }

}
