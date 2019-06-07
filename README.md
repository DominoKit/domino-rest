![GWT3/J2CL compatible](https://img.shields.io/badge/GWT3/J2CL-compatible-brightgreen.svg)
[![Build Status](https://travis-ci.org/DominoKit/domino-rest.svg?branch=master)](https://travis-ci.org/DominoKit/domino-rest)

# domino-rest

Domino-rest is a lib for generating rest clients from **JaxRs** compatible interfaces, and the generated clients can be used from both client side and server in GWT applications and works in both **GWT2** and **GWT3**,
the serialization and deserialization is based on [domino-jackson](https://github.com/DominoKit/domino-jackson), and service definition is based on **JaxRs** annotations, while code generation uses annotation processing.

## Setup

### Maven dependency

Domino-rest can be used in two modes : 

1- Interface defined in client/server module, then we only add dependencies to client/server module only

 - Client dependencies
```xml
<!-- Lib dependency-->
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-rest-gwt</artifactId>
  <version>1.0-rc.4-SNAPSHOT</version>
</dependency>

<!-- Annotation processor dependency-->
<dependency>
    <groupId>org.dominokit</groupId>
    <artifactId>domino-rest-apt</artifactId>
    <version>1.0-rc.4-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

 - Server dependencies
```xml
<!-- Lib dependency-->
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-rest-server</artifactId>
  <version>1.0-rc.4-SNAPSHOT</version>
</dependency>

<!-- Annotation processor dependency-->
<dependency>
    <groupId>org.dominokit</groupId>
    <artifactId>domino-rest-apt</artifactId>
    <version>1.0-rc.4-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

2- Interface is defined in the shared module, then we add dependencies to both shared module and client/server module.

   - In client module we add
   
   ```xml
   <!-- Lib dependency-->
   <dependency>
     <groupId>org.dominokit</groupId>
     <artifactId>domino-rest-gwt</artifactId>
     <version>1.0-rc.4-SNAPSHOT</version>
   </dependency>
   ```
  - In server module we add
  
  ```xml
  <!-- Lib dependency-->
  <dependency>
    <groupId>org.dominokit</groupId>
    <artifactId>domino-rest-server</artifactId>
    <version>1.0-rc.4-SNAPSHOT</version>
  </dependency>
  ```
   
   - In shared module we add
   ```xml
   <!-- Lib dependency-->
   <dependency>
     <groupId>org.dominokit</groupId>
     <artifactId>domino-rest-shared</artifactId>
     <version>1.0-rc.4-SNAPSHOT</version>
   </dependency>
   
   <!-- Annotation processor dependency-->
   <dependency>
       <groupId>org.dominokit</groupId>
       <artifactId>domino-rest-apt</artifactId>
       <version>1.0-rc.4-SNAPSHOT</version>
       <scope>provided</scope>
   </dependency>
   ```
   

> To use the snapshot version without building locally, configure the snapshot repository
```xml
<repository>
   <id>sonatype-snapshots-repo</id>
   <url>https://oss.sonatype.org/content/repositories/snapshots</url>
   <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
      <checksumPolicy>fail</checksumPolicy>
   </snapshots>
</repository>
```

### GWT module inheritance
```xml
<inherits name="org.dominokit.domino.rest.GwtRest"/>
```
### Usage

#### Initializing the context

First step to start working with domino-rest is to initialize domino-rest context which will inject some implementation depending on where are using it (client/server), the domino-rest context can be initialized with recommended defaults using
 
```
DominoRestConfig.initDefaults();
```

#### Write the pojos

A pojo used in the service definition as a response or request needs to be annotated with `@JSONMapper` in order to generate the JSON mappers for it, we will see later how we can customize this.

```java
@JSONMapper
public class Movie {

    private String name;
    private int rating;
    private String bio;
    private String releaseDate;

    // setters and getters
}
```

#### Write the service definition

To define a rest service create an interface and annotate it with `@RequestFactory` which will trigger the annotation processor when we compile to generate the rest client.
Add as many methods annotated using JaxRs annotations, and the processor will create a request class and a factory method to execute that method and call the server. 


```java
@RequestFactory
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    MovieResponse getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    List<Movie> listMovies();

    @Path("library/movies/:name")
    @PUT
    void updateMovie(@RequestBody Movie movie);
}
```

Any pojo used in the service response or request needs to be annotated with `@JSONMapper` in order to enable JSON serialization/deserialization.

#### Use the generated client

The generated client class will be named with the service interface name + "Factory", get the instance and call the service method :

```java
MoviesServiceFactory.INSTANCE
    .getMovieByName("hulk")
    .onSuccess(movie -> {
        //do something on success
    })
    .onFailed(failedResponse -> {
        //do something on error
    })
    .send();

MoviesServiceFactory.INSTANCE
    .listMovies()
    .onSuccess(movies -> {
        //do something on success
    })
    .onFailed(failedResponse -> {
        //do something on error
    })
    .send();
    
MoviesServiceFactory.INSTANCE
    .updateMovie(movie)
    .onSuccess(aVoid -> {
        //do something on success
    })
    .onFailed(failedResponse -> {
        //do something on error
    })
    .send();

```

### Customizations and Configurations

#### Service Root

- ##### Global service root

By default domino-rest assumes that the rest points are deployed to the same host and port of the running application, so if the application is running on `localhost` at port `8080` then all services will be mapped to :

`http://localhost:8080/service/{path to service}`

we can change the default service root globally for all services using the `DominoRestConfig` class : 

```java
DominoRestConfig.getInstance()
				.setDefaultServiceRoot("http://127.0.0.1:9090/");

```

after changing the service root all service will be mapped to the new service root .e.g: `http://127.0.0.1:9090/{path to service}`

 - ##### Service root for a single service
 
 We can change the service root for any service while keeping other services mapped to the default service root using the `@ServiceFactory` annotation by setting the `serviceRoot` attribute
 
 ```java
@RequestFactory(serviceRoot = "http://localhost:7070/library/")
public interface MoviesService {

    @Path("movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("movies")
    @GET
    List<Movie> listMovies();

    @Path("movies/:name")
    @PUT
    void updateMovie(@RequestBody Movie movie);
}
```

with this we can make the movies service for example map to port `7070` while keep other services map to default port `8080`


- ##### Dynamic service root mapping

Instead of fixed service mapping for each service, or using one global service mapping for all service domino-rest allows mapping service to different roots based on some matching conditions.
for example we want all services that has a path starts with `movies` to map to `http://localhost:7070/library/` while all services with paths starts with `books` map to `http://localhost:9090` and so on.
this is also very useful when the service roots are not fixed and could be defined as system properties or coming from sort of configuration.

in order to define a dynamic service root we use the `DominoRestConfig` class 

```java
DominoRestConfig.getInstance()
    .addDynamicServiceRoot(DynamicServiceRoot
            .pathMatcher(path -> path.startsWith("movies"))
            .serviceRoot(() -> "http://localhost:7070/library/")
    )
    .addDynamicServiceRoot(DynamicServiceRoot
            .pathMatcher(path -> path.startsWith("books"))
            .serviceRoot(() -> "http://localhost:9090")
    );
```

Any service that isn't matched with of the defined matcher will be mapped to the default service root.

We can also use dynamic service roots to remove the host and port mapping from the service definition while keeping using a custom service root for that interface

for example instead of defining the movies service like this :
```java
@RequestFactory(serviceRoot = "http://localhost:7070/")
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    List<Movie> listMovies();

    @Path("library/movies/:name")
    @PUT
    void updateMovie(@RequestBody Movie movie);
}
```
We can define it like this

```java
@RequestFactory(serviceRoot = "library/")
public interface MoviesService {

    @Path("movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("movies")
    @GET
    List<Movie> listMovies();

    @Path("movies/:name")
    @PUT
    void updateMovie(@RequestBody Movie movie);
}
```

then we define a service root like this :

```java
DominoRestConfig.getInstance()
    .addDynamicServiceRoot(DynamicServiceRoot
            .pathMatcher(path -> path.startsWith("library"))
            .serviceRoot(() -> "http://localhost:7070/library/")
    )
```

notice now how we dont have the host and port hard-coded in the service definition, and how we have a shorter path mapping in the service methods.

this will map for example the `getMovieByname` method to `http://localhost:7070/library/movies/hulk`.

#### Resource root

By default when a service is mapped using the default service root it will be mapped to the resource root as `service` meaning that it will be mapped to an endpoint path that starts with `service`.
we can override this using `DominoRestConfig` : 

```java
DominoRestConfig.getInstance()
                .setDefaultResourceRootPath("endpoint");
```

now when a service is mapped to default service root it will be mapped to for example `http://localhost:8080/endpoint/{path from method @Path annotation}`

The resource root path will only work with default service root, and will be ignored for services that override the service root or when we override the service root globally.


#### HTTP Methods

We can set the http request method on a service method using one of the JaxRs annotations : `@GET`,`@POST`,`@PUT`,`@PATCH`,`@DELETE`,`@HEAD`,`@OPTIONS`.

if none of these annotations is present in the service method definition then it is considered `GET` by default, and only `@POST`, `@PUT`, `@PATCH` will allow sending a body in the request while the other will ignore any body presented in the request or service definition.

#### Service method path mapping

The JaxRs annotation `@Path` is used to map each service method to an endpoint in the service alongside the http method, the path defined in the service method definition will be appended to matched service root of that service and can have variable parameters.

for example for the service :

```java
@RequestFactory
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    List<Movie> listMovies();

    @Path("library/movies/:name")
    @PUT
    void updateMovie(@RequestBody Movie movie);
}
```

the `listMovies` method will produce an http request to the following endpoint :

`http://localhost:8080/service/library/movies`

we can define a variable parameter in the method path by adding `:` before the name of the parameter or surround it with `{}`, the parameter name will be matched with the method argument name for replacement

for example : calling `getMovieByName` and pass the movie name `hulk` as argument will result in the following http request path :

`http://localhost:8080/service/library/movies/hulk`

if we are passing a request body in the method argument we can use that request properties to replace path variable parameters, the variable path parameter name will be match with the property name from the request pojo.

#### Request body

In case of `POST`, `PUT`, `PATCH` http requests we normally sends a body in the request, in domino-rest the body of the request is determined from the call argument using one of the following in order :

1- The argument is implementing the marker interface `RequestBean`.

2- The argument in the method is annotated with `@RequestBody`.

3- The class representing the type of the argument is annotated with `@RequestBody`.

4- The class representing the type of the argument is annotated with `@JSONMapper`.

#### Produces & Consumes

Domino-rest use JaxRs `@Produces` and `@Consumes` and By default all service methods are mapped to produce or consume `MediaType.APPLICATION_JSON` which is omitted, since the default serialization/deserialization in domino-rest is JSON, 
and using any other format will require writing custom serializer/deserializers using `@Writer` and `@Reader` annotations :

- ##### Writer

if want a service method to send the body in a format other than JSON we can write a custom serializer or writer by implementing the generic interface `RequestWriter<T>` 

e.g if want the update method to send the movie in the body in `xml` format instead of JSON, we introduce a writer class :

```java
    public class XmlMovieWriter implements RequestWriter<Movie>{
        @Override
        public String write(Movie request) {
            String movieXml = //convert the movie to xml
            return movieXml;
        }
    }
```

then in our service definition, we change the `@Consumes` and specify the writer using the `@Writer` annotation 

```java
@RequestFactory
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    List<Movie> listMovies();

    @Path("movies/:name")
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Writer(MovieXmlWriter.class)
    void updateMovie(@RequestBody Movie movie);
}
```

#### Success codes

By default status codes `200`,`201`,`202`,`203`,`204` are considered a success for all requests, if we need to change this behavior we can change this for a single request using the `@SuccessCodes` annotation.

```java
@RequestFactory
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    List<Movie> listMovies();

    @Path("library/movies/:name")
    @PUT
    @SuccessCodes({200})
    void updateMovie(@RequestBody Movie movie);
}
```

now `updateMethod` will only be considered success only of the response status code is `200`.

#### Timeout and maximum retries

Sometimes requests might timeout due to network latency or other reasons and we dont want our requests to fail directly but rather want to retry several times before we end up failing the request.
in domino-rest we can use the `@Retries` annotation to define a timeout with maximum retries count.

```java
@RequestFactory
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    Movie getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    List<Movie> listMovies();

    @Path("library/movies/:name")
    @PUT
    @SuccessCodes({200})
    @Retries(timeout=3000, maxRetries=5)
    void updateMovie(@RequestBody Movie movie);
}
```

the `updateMovie` will timeout if the response didnt return within 3000 milliseconds but will try 5 times before it actually fail.

we can also set a global timeout and max retries in a global interceptor.

#### Setting request URL

In some cases like when we work with HATEOAS links the request url isnt fixed and is received from the response of another rest request and so using service root or Path mapping does not work,
in this case we can leave the value for `@Path` annotation empty and use the request `setUrl` method to set the url, this method will override any other path setup.

Example 

```java
MoviesServiceFactory.INSTANCE
    .updateMovie(movie)
    .setUrl("http://localhost:6060/movies")
    .onSuccess(aVoid -> {
        //do something on success
    })
    .onFailed(failedResponse -> {
        //do something on error
    })
    .send();
```


#### Global interceptors

In many cases we might need to intercept all rest requests to add some extra headers, like security headers or authentication tokens, and it would be painful to do this for each request one at a time.
and for this domino-rest allow defining global interceptors that can intercept all requests using `DominoRestConfig`, we can define global interceptors like the following :

```java
public class TokenInterceptor implements RequestInterceptor {
    @Override
    public void interceptRequest(ServerRequest request, ContextAggregator.ContextWait<ServerRequest> contextWait) {
        request.setHeader("Authorization", "some token goes here");
        contextWait.complete(request);
    }
}
```

The request interceptors are blocking which allows us to do some other rest calls before or async operation and only send the request after all interceptors calls the complete method of the contextWait received in the argument.