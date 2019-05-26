package org.dominokit.domino.rest;

import org.dominokit.jacksonapt.annotation.JSONMapper;

@JSONMapper
public class SampleResponse {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
