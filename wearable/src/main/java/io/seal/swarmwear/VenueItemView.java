package io.seal.swarmwear;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class VenueItemView extends FrameLayout implements WearableListView.Item {

    private final CircledImageView imgView;
    private final TextView txtView;

    private final float mNormalCircleRadius;
    private final float mSelectedCircleRadius;
    private final int whiteColor;
    private final int orangeColor;
    private float mScale;

    public VenueItemView(Context context) {
        super(context);

        mNormalCircleRadius = getResources().getDimension(R.dimen.normal_circle_radius);
        mSelectedCircleRadius = getResources().getDimension(R.dimen.selected_circle_radius);

        View.inflate(context, R.layout.list_item_venue, this);
        imgView = (CircledImageView) findViewById(R.id.img);
        txtView = (TextView) findViewById(R.id.txtName);
        orangeColor = getResources().getColor(R.color.orange_normal);
        whiteColor = getResources().getColor(R.color.white);
    }

    @Override
    public float getProximityMinValue() {
        return mNormalCircleRadius;
    }

    @Override
    public float getProximityMaxValue() {
        return mSelectedCircleRadius;
    }

    @Override
    public float getCurrentProximityValue() {
        return mScale;
    }

    @Override
    public void setScalingAnimatorValue(float value) {
        mScale = value;
        imgView.setCircleRadius(mScale);
        imgView.setCircleRadiusPressed(mScale);
    }

    @Override
    public void onScaleUpStart() {
        imgView.setAlpha(1f);
        txtView.setAlpha(1f);
        imgView.setCircleBorderColor(whiteColor);
        imgView.setCircleColor(orangeColor);
    }

    @Override
    public void onScaleDownStart() {
        imgView.setAlpha(0.5f);
        txtView.setAlpha(0.5f);
        imgView.setCircleBorderColor(whiteColor);
        imgView.setCircleColor(orangeColor);
    }
}