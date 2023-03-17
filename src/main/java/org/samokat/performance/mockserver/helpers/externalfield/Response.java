package org.samokat.performance.mockserver.helpers.externalfield;

import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("variables")
    private Variables variables;

    @SerializedName("query")
    private String query;

    @SerializedName("operationName")
    private String operationName;


    public Variables getVariables() {
        return variables;
    }


    public String getQuery() {
        return query;
    }


    public String getOperationName() {
        return operationName;
    }
}
