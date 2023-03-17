package org.samokat.performance.mockserver.helpers.externalfield;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Variables {

    @SerializedName("representations")
    private List<RepresentationsItem> representations;


    public List<RepresentationsItem> getRepresentations() {
        return representations;
    }
}
