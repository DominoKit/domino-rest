# domino-rest

## Setup

### Maven dependency

Domino-rest can be used in two modes : 

1- Interface defined in client module, then we only add dependencies to client module only 

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

2- Interface is defined in the shared module, then we add dependencies to both shared module and client module.

   - In client module we add
   
   ```xml
   <!-- Lib dependency-->
   <dependency>
     <groupId>org.dominokit</groupId>
     <artifactId>domino-rest-gwt</artifactId>
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
<inherits name="org.dominokit.rest.GwtRest"/>
```
### Usage

Domino-res is built based on both `domino-jackson` for serialization/de-serialization of requests and responses, and provides a simple declarative way to define the server calls, 
you can create a service interface and add `jax-rs` annotations and it will generate the classes to do the server call, leaving to you the handling of success and failure of requests.

Sample service :

```java
@RequestFactory
public interface MoviesService {

    @Path("library/movies/:movieName")
    @GET
    Response<MovieResponse> getMovieByName(String movieName);

    @Path("library/movies")
    @GET
    Response<ArrayResponse<Movie>> listMovies();

    @Path("library/movies/:name")
    @PUT
    Response<VoidResponse> updateMovie(Movie movie);
}
```

the interface is annotated with `@RequestFactory` this will cause the generation of a factory class to create new requests instance based on the interface methods annotated with `@Path`, the path annotation defines what resource this request should be calling on the server, so this is a mapping to the server endpoints.

by default the request method is `GET`, the path value can have variable parameters, and those will be substituted from the request being sent to the server, you also can use primitives and wrapper types as method parameters that can be substituted in the path , if the method allows sending a body to the server then the request will be serialized and sent to the server as a request body, otherwise it will only be used to replace the path variable parameters.

You can define one method parameter to be the request body by annotating the parameter with `@RequestBody`, or annotating the request class itself with `@RequestBody` or `@JSONMapper`

the return type of any method annotated with the `@Path` should be of type `Response` with a generic type.

both request and response can be annotated with `@JSONMapper` to generate serializer/deserializer for each, or we can define custom serialisers/deserialsers using the `@Writer`/`@Reader` annotation.

you can always leave the interface method parameters empty and the framework will by default use a predefined `VoidRequest` instance.

defining the response as a generic type of `ArrayResponse` will expect the result to be a JSON array, and it will result in receiving an array of your response bean.

When you dont expect a body in the response you can define the response of type `VoidReponse`.


Sample request and response : 

**Request**

```java
@JSONMapper
public class MovieRequest {

    private String movieName;

    public MovieRequest() {
    }

    public MovieRequest(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
}
```

**Response**

```java
@JSONMapper
public class Movie {

    private String name;
    private int rating;
    private String bio;
    private String releaseDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}

```

#### Define the service

```java
@RequestFactory
public interface MoviesService {

    @Path("movies/:movieName")
    @GET
    Response<Movie> getMovieByName(@RequestBody MovieRequest movieRequest);

    @Path("movies")
    @GET
    Response<ArrayResponse<Movie>> listMovies();

    @Path(value = "movies/:name")
    @PUT
    Response<VoidResponse> updateMovie(Movie movie);
}
```

#### Using the service 

The generated factory class will have the interface name followed by `Factory` so for the sample above it will be `MoviesServiceFactory`, we will get an instance from this factory and use it to call our service.

Sample : 

```java
public void onMovieSelected(String movieName) {

    MoviesServiceFactory.INSTANCE.getMovieByName(new MovieRequest(movieName))
            .onSuccess(movie -> {
                LOGGER.info("movie loaded from server : " + movie.toString());
            })
            .onFailed(failedResponse -> {
                LOGGER.info("Failed to load movie : " + failedResponse.getStatusCode());
            })
            .send();

}
```

once the send is called the request will be sent to the server, and we can handle the results in the `onSuccess` and `onFailed` handlers.

#### Service Root

You can specify a root path for your services using the request factory annotation `@RequestFactory(serviceRoot = "library")`, this path will be automatically appended to all requests paths in this service and can be omitted from the `@Path` annotation. Sample

```java
@RequestFactory(serviceRoot = "library/")
public interface MoviesService {

    @Path("movies/:movieName")
    @GET
    Response<Movie> getMovieByName(String movieName);

    @Path("movies")
    @GET
    Response<ArrayResponse<Movie>> listMovies();

    @Path(value = "movies/:name")
    @PUT
    Response<VoidResponse> updateMovie(Movie movie);
}
```

By default all requests will be mapped to the predefined end-point `service`, for the example the above example requests will be mapped in the server if we are running on localhost:8080 to `http://localhost:8080/service/library/movies...`.

this default behavior can be customized using `GwtRestConfig`, you can obtain a `GwtRestConfig` instance by calling `GwtRestConfig.getInstance()`, once the obtained you can use this config instance to register a dynamic service root which allows different mapping for different request to different end-points even out side your application.

Sample : 

```java
GwtRestConfig.getInstance()
        .addDynamicServiceRoot(DynamicServiceRoot
        .pathMatcher(path -> path.startsWith("library"))
        .serviceRoot(() -> "http://localhost:9090/cinema/"));
```

you can do this in an initialization phase of your application, then when we make our requests then the paths of the requests will be matched using the path matchers and the first matching service root will be used to execute the request, if no service root matches the request path it will fallback to the default behavior and use the `service` root.

you can add as many dynamic service roots as needed.