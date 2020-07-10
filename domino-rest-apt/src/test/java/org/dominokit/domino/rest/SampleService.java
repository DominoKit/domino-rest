package org.dominokit.domino.rest;


import org.dominokit.domino.rest.shared.request.service.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RequestFactory
@Path("library/")
public interface SampleService {

    @GET
    @Path("someService/:id")
    SampleResponse getById(@RequestBody int id, int count);

    @POST
    @Path("someService/create")
    @Produces(MediaType.APPLICATION_JSON)
    @WithCredentials(true)
    Void registerUser(@RequestBody HashMap<String, String> personalData);

    @POST
    @Path("somePath/:name")
    @WithCredentials(false)
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
    @Path("someService/{starting-date}")
    String getByDate(@QueryParam("birth-date") @DateFormat("dd-MM-yyyy") Date birthDate, @PathParam("starting-date") Date startingDate);

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

    @Path("path0")
    interface Int0{
        @Path("00")
        void get0();
    }

    @Path("path1")
    interface Int1 extends Int0{
        @Path("11")
        void get1();
    }

    @Path("path2")
    interface Int2{
        @Path("22")
        void get2();
    }

    @Path("pathX")
    @RequestFactory
    interface IntX extends Int1, Int2{
        @Path("xx")
        void getX();
    }

}
