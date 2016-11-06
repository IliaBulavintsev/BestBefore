package com.rv150.bestbefore.Models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rudnev on 05.11.2016.
 */

public class Group {
    private String name;
    private long id;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public JSONObject getJSON() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("name", getName());
        result.put("id", getId());
        return result;
    }
}