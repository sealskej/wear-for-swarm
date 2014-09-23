package io.seal.swarmwear.lib.model;

import android.os.Parcel;

public class Icon {

    private String prefix;
    private String suffix;

    public Icon(Parcel in) {
        prefix = in.readString();
        suffix = in.readString();
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

}
