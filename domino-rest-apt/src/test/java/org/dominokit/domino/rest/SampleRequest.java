package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.request.service.annotations.RequestBody;
import org.dominokit.jacksonapt.annotation.JSONMapper;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@JSONMapper
@RequestBody
public class SampleRequest{

    @PathParam("name")
    private String name;
    private String title;
    @QueryParam("name2")
    public String anotherName;
    @HeaderParam("desc")
    public String description;

    private NestedRequestBean accountInfo;

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

    public String getAnotherName() {
        return anotherName;
    }

    public void setAnotherName(String anotherName) {
        this.anotherName = anotherName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NestedRequestBean getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(NestedRequestBean accountInfo) {
        this.accountInfo = accountInfo;
    }
}
