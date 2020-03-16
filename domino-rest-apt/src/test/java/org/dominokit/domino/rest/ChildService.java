package org.dominokit.domino.rest;

import javax.ws.rs.PathParam;

public interface ChildService {
    String getById(@PathParam("id") int id);
    String getById(@PathParam("id") String id);
    String anotherGetById(@PathParam("id") int id);
}
