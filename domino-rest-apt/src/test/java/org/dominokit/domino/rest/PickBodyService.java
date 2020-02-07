package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.request.service.annotations.RequestBody;
import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;

@RequestFactory
public interface PickBodyService {

    @Path("/")
    String methoda(SampleDTO sampleDTO);

    @Path("/")
    String methodb(String test, SampleDTO sampleDTO);

    @Path("/")
    String methodc(String test, String test2);

    @Path("/{test}/{test3}")
    String methodd(@PathParam("test") String test, @HeaderParam("test5") String test5, @QueryParam("test2") String test2, @Context String test3, @Suspended String test4);

    @Path("/{test3}")
    String methode(String test, @QueryParam("test2") String test2, String test3, @RequestBody String test4);

    @Path("/")
    SampleDTOX methodf(SampleDTO sampleDTO);
}
