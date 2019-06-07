package org.dominokit.domino.rest;


import org.dominokit.domino.rest.shared.request.service.annotations.Classifier;
import org.dominokit.domino.rest.shared.request.service.annotations.RequestBody;
import org.dominokit.domino.rest.shared.request.service.annotations.RequestFactory;
import org.dominokit.domino.rest.shared.request.service.annotations.Retries;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;

@RequestFactory
public interface SampleService {

    @GET
    @Path("someService/:id")
    SampleResponse getById(@RequestBody int id);

    @POST
    @Path("somePath/:name")
    SampleResponse annotatedBody(SampleRequest sampleRequest);

    @POST
    @Path("somePath/{name}")
    SampleResponse annotatedBody2(SampleRequest sampleRequest);

    @GET
    @Path("someService/:id")
    SampleResponse getById(String id);

    @GET
    @Path("someService/{id}")
    @Classifier("long")
    void getById(Long id);

    @GET
    @Path("someService/:id")
    @Classifier("double")
    int getById(Double id);

    @GET
    @Path("someService/:id")
    List<String> getById(double id);

    @GET
    @Path("someService/:id")
    String[] getById6(int id);


    @GET
    @Path("someService")
    String getByIdQuery(@QueryParam("userId") int id, @QueryParam("userName") String name);

    @GET
    @Path("someService/:id")
    int[] getById7(int id);

    @GET
    @Path("someService/{id}")
    int[][] getById8(int id);

    @GET
    @Path("someService/:id")
    @Retries(timeout = 3000, maxRetries = 5)
    int[][] getByI98(int id);

}
