package io.seal.swarmwear;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.TextView;
import io.seal.swarmwear.lib.model.Venue;

import java.util.List;

public class VenuesAdapter extends WearableListView.Adapter {


    private final List<Venue> mVenuesList;

    public VenuesAdapter(List<Venue> venueList) {
        this.mVenuesList = venueList;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(new VenueItemView(viewGroup.getContext()));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        VenueItemView itemView = (VenueItemView) holder.itemView;

        Venue venue = mVenuesList.get(position);

        TextView txtView = (TextView) itemView.findViewById(R.id.txtName);
        txtView.setText(venue.getName());

        Bitmap bitmap = venue.getPrimaryCategoryBitmap();
        if (bitmap != null) {
            CircledImageView imgView = (CircledImageView) itemView.findViewById(R.id.img);
            Resources resources = itemView.getContext().getResources();
            imgView.setImageDrawable(new BitmapDrawable(resources, bitmap));
        }

        itemView.setTag(venue.getId());
    }

    @Override
    public int getItemCount() {
        return mVenuesList.size();
    }

    public List<Venue> getVenuesList() {
        return mVenuesList;
    }
}