package io.seal.swarmwear.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import com.squareup.otto.Subscribe;
import io.seal.swarmwear.BaseListFragment;
import io.seal.swarmwear.BusProvider;
import io.seal.swarmwear.EventManager;
import io.seal.swarmwear.R;
import io.seal.swarmwear.Utils;
import io.seal.swarmwear.event.VenuesAvailableEvent;
import io.seal.swarmwear.lib.Properties;
import io.seal.swarmwear.lib.model.Venue;
import io.seal.swarmwear.networking.Foursquare;
import io.seal.swarmwear.service.SearchVenuesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListNearbyVenuesFragment extends BaseListFragment
        implements AdapterView.OnItemClickListener {

    public static final String TAG = "ListNearbyVenuesFragment";

    public static final String NAME_COLUMN = "col_1";
    public static final String ADDRESS_COLUMN = "col_2";
    public static final String VENUE_ID_COLUMN = "col_3";

    private ArrayList<HashMap<String, String>> fillMaps;
    private SimpleAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        String[] from = new String[]{NAME_COLUMN, ADDRESS_COLUMN, VENUE_ID_COLUMN};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        fillMaps = new ArrayList<>();

        mAdapter = new SimpleAdapter(getActivity(), fillMaps, android.R.layout.simple_list_item_2, from, to);

        setListAdapter(mAdapter);

    }

    private void fillVenuesFromIntent(Bundle intent) {

        fillMaps.clear();

        String[] idArray = intent.getStringArray(Properties.Keys.VENUE_ID_ARRAY);
        String[] namesArray = intent.getStringArray(Properties.Keys.VENUE_NAMES_ARRAY);
        String[] addressArray = intent.getStringArray(Properties.Keys.VENUE_ADDRESS_ARRAY);

        if (idArray == null || idArray.length == 0 || namesArray == null ||
                namesArray.length == 0 || addressArray == null || addressArray.length == 0) {

            setEmptyText(R.string.no_venues);
        } else {

            for (int i = 0; i < idArray.length; i++) {
                addMapToList(idArray[i], namesArray[i], addressArray[i]);
            }
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (savedInstanceState != null) {

            fillVenuesFromIntent(savedInstanceState);
            setListShown(true);

        } else if (intent.hasExtra(Properties.Keys.VENUE_ID_ARRAY)) {

            fillVenuesFromIntent(intent.getExtras());
            setListShown(true);

        } else {

            if (Utils.isNetworkConnectedOrConnecting(getActivity())) {
                setListShown(false);
                searchVenuesIfLoggedIn();
            } else {
                setListShown(true);
                setEmptyText(R.string.no_internet_connection);
            }
        }

        mAdapter.notifyDataSetChanged();

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventManager.trackScreenView(TAG);
        BusProvider.getInstance().register(this);
        SearchVenuesService.start(getActivity());
    }

    @Override
    public void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }

    @Subscribe
    @SuppressWarnings("UnusedDeclaration")
    public void venuesDownloaded(VenuesAvailableEvent event) {
        EventManager.trackAndLogEvent(TAG, "venuesDownloaded");

        ArrayList<Venue> allVenues = event.getVenues();

        fillMaps.clear();

        if (allVenues.isEmpty()) {
            setEmptyText(R.string.no_venues);

        } else {
            for (Venue venue : allVenues) {

                String name = venue.getName();
                String address = venue.getLocation().getAddress();
                String id = venue.getId();

                addMapToList(id, name, address);
            }
        }

        mAdapter.notifyDataSetChanged();

        setListShown(true);
    }

    private void addMapToList(String venueId, String name, String address) {
        HashMap<String, String> map = new HashMap<>();
        map.put(NAME_COLUMN, name);
        map.put(ADDRESS_COLUMN, address);
        map.put(VENUE_ID_COLUMN, venueId);
        fillMaps.add(map);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        BaseAdapter adapter = (BaseAdapter) adapterView.getAdapter();
        Map map = (Map) adapter.getItem(position);

        String venueId = (String) map.get(VENUE_ID_COLUMN);

        CheckinConfirmationFragment.newInstance(venueId).show(getFragmentManager(), null);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.refresh) {
            setListShown(false);
            SearchVenuesService.start(getActivity());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int size = fillMaps.size();

        if (fillMaps.isEmpty()) {
            return;
        }

        String[] nameArray = new String[size];
        String[] addressArray = new String[size];
        String[] venueIdArray = new String[size];

        for (int i = 0; i < fillMaps.size(); i++) {
            HashMap<String, String> map = fillMaps.get(i);
            nameArray[i] = map.get(NAME_COLUMN);
            addressArray[i] = map.get(ADDRESS_COLUMN);
            venueIdArray[i] = map.get(VENUE_ID_COLUMN);
        }

        Venue.fillBundle(outState, nameArray, addressArray, venueIdArray);

    }

    private void searchVenuesIfLoggedIn() {
        if (Foursquare.isLoggedIn(getActivity())) {
            SearchVenuesService.start(getActivity());
        }
    }

    private void setEmptyText(int resId) {
        setEmptyText(getString(resId));
    }
}
