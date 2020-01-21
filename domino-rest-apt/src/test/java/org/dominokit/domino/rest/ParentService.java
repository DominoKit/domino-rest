package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@RequestFactory
@Path("sample")
public interface ParentService extends ChildService,AnotherChildService{

    @Override
    @GET
    @Path("/test")
    String getById(@QueryParam("id") int id);
}
