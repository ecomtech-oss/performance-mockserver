package org.samokat.performance.mockserver.helpers.externalfield;

import com.google.gson.annotations.SerializedName;

public class RepresentationsItem {

    @SerializedName("__typename")
    private String typename;


    public String getTypename() {
        return typename;
    }

}
