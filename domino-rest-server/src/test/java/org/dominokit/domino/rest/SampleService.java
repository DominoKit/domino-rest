package org.dominokit.domino.rest;

import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@RequestFactory
public interface SampleService {

    @GET
    @Path("/greet")
    String sayHello();
}
