package io.seal.swarmwear.lib.model;

import android.graphics.Bitmap;
import android.os.Bundle;
import com.google.android.gms.wearable.DataMap;
import io.seal.swarmwear.lib.Properties;

import java.util.List;

public class Venue {

    public static final String ID = "id";
    public static final String NAME = "name";

    private String id;
    private String name;
    private Location location;
    @SuppressWarnings("UnusedDeclaration")
    private List<Category> categories;
    private Bitmap primaryCategoryBitmap;

    public Venue() {
    }

    public Venue(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static void fillBundle(Bundle bundle, String[] nameArray, String[] addressArray, String[] venueIdArray) {
        bundle.putStringArray(Properties.Keys.VENUE_NAMES_ARRAY, nameArray);
        bundle.putStringArray(Properties.Keys.VENUE_ADDRESS_ARRAY, addressArray);
        bundle.putStringArray(Properties.Keys.VENUE_ID_ARRAY, venueIdArray);
    }

    public static Venue extractFromDataMap(DataMap dataMap) {
        String id = dataMap.getString(ID);
        String name = dataMap.getString(NAME);
        return new Venue(id, name);
    }

    public DataMap getDataMap() {
        final DataMap dataMap = new DataMap();
        dataMap.putString(ID, this.id);
        dataMap.putString(NAME, this.name);
        return dataMap;
    }

    public String getPrimaryCategoryPNGIconUrl() {

        List<Category> categoriesList = getCategories();

        if (categoriesList != null) {
            for (Category category : categoriesList) {
                Icon icon = category.getIcon();
                if (category.isPrimary() && ".png".equals(icon.getSuffix().toLowerCase())) {
                    return icon.getPrefix() + "44" + icon.getSuffix();
                }
            }
        }

        return null;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Bitmap getPrimaryCategoryBitmap() {
        return primaryCategoryBitmap;
    }

    public void setPrimaryCategoryBitmap(Bitmap primaryCategoryBitmap) {
        this.primaryCategoryBitmap = primaryCategoryBitmap;
    }

    @Override
    public String toString() {
        return "Venue{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                '}';
    }

}
