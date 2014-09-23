package io.seal.swarmwear.event;

import io.seal.swarmwear.lib.model.Venue;

import java.util.ArrayList;

public class VenuesAvailableEvent {

    private final ArrayList<Venue> mVenues;

    public VenuesAvailableEvent(ArrayList<Venue> venues) {
        mVenues = venues;
    }

    public ArrayList<Venue> getVenues() {
        return mVenues;
    }

}
