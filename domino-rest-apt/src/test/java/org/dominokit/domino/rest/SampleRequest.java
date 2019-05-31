package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.request.service.annotations.RequestBody;
import org.dominokit.jacksonapt.annotation.JSONMapper;

@JSONMapper
@RequestBody
public class SampleRequest{

    private String name;
    private String title;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
