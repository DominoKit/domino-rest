![logoimage](https://raw.githubusercontent.com/DominoKit/DominoKit.github.io/master/logo/128.png)

<a title="elements" href="https://matrix.to/#/#DominoKit_domino:gitter.im"><img src="https://badges.gitter.im/Join%20Chat.svg"></a>
[![Development Build Status](https://github.com/DominoKit/domino-rest/actions/workflows/deploy.yaml/badge.svg?branch=development)](https://github.com/DominoKit/domino-rest/actions/workflows/deploy.yaml/badge.svg?branch=development)
![https://central.sonatype.com/artifact/org.dominokit/domino-rest/overview](https://img.shields.io/github/v/release/DominoKit/domino-rest.svg?include_prereleases=&sort=semver&color=14c398)
![Sonatype Nexus (Snapshots)](https://img.shields.io/badge/Snapshot-HEAD--SNAPSHOT-orange)
![GWT3/J2CL compatible](https://img.shields.io/badge/GWT3/J2CL-compatible-brightgreen.svg)

# domino-rest

Domino-rest generates REST clients from JAX-RS interfaces. The same generated client works in the browser (GWT2 or GWT3/J2CL) and on the JVM. JSON serialization uses [domino-jackson](https://github.com/DominoKit/domino-jackson), and clients are generated at compile time using annotation processing (APT).

## Highlights

- Generate fluent REST clients from JAX-RS interfaces.
- Use the same client in browser and JVM environments.
- Built-in JSON mapping with domino-jackson and optional custom readers/writers.
- Flexible configuration (service roots, interceptors, retries, and more).

## Modules

- `domino-rest-client`: browser/GWT/J2CL runtime implementation.
- `domino-rest-jvm`: JVM runtime implementation.
- `domino-rest-shared`: shared request model, annotations, and utilities.
- `domino-rest-processor`: annotation processor that generates request factories.
- `domino-rest-jaxrs`: minimal JAX-RS API shim for environments without full JAX-RS.
- `domino-rest-test`, `domino-rest-test-java17`, `domino-rest-client-test`: test modules.

## Prerequisites

- Java 11+
- Maven 3.6+

## Quick start

Add dependencies:

```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-rest-client</artifactId>
  <version>2.0.0</version>
</dependency>
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-rest-processor</artifactId>
  <version>2.0.0</version>
  <scope>provided</scope>
</dependency>
```

If you configure annotation processors explicitly, include the processor:

```xml
<annotationProcessorPaths>
  <path>
    <groupId>org.dominokit</groupId>
    <artifactId>domino-rest-processor</artifactId>
    <version>2.0.0</version>
  </path>
</annotationProcessorPaths>
```

For GWT/J2CL add the inherit directive:

```xml
<inherits name="org.dominokit.rest.Rest"/>
```

Initialize Domino REST at startup:

```java
DominoRestConfig.initDefaults();
```

## Define a service

Annotate a JAX-RS interface with `@RequestFactory` and use JAX-RS annotations:

```java
@RequestFactory
public interface MoviesService {

  @Path("library/movies/:movieName")
  @GET
  Movie getMovieByName(@PathParam("movieName") String movieName);

  @Path("library/movies")
  @GET
  List<Movie> listMovies();

  @Path("library/movies/:name")
  @PUT
  void updateMovie(@BeanParam @RequestBody Movie movie);
}
```

The generated client name is the interface name plus `Factory`:

```java
MoviesServiceFactory.INSTANCE
    .getMovieByName("hulk")
    .onSuccess(movie -> { })
    .onFailed(failedResponse -> { })
    .send();
```

For POJOs used in requests or responses, add `@JSONMapper` to reuse generated mappers:

```java
@JSONMapper
public class Movie {
  @PathParam("name")
  private String name;
  private int rating;
  private String bio;
  private String releaseDate;
}
```

## Configuration

### Service root

By default, requests target the host where the app is served with a `service/` prefix. You can override this globally:

```java
DominoRestConfig.getInstance()
    .setDefaultServiceRoot("http://127.0.0.1:9090/");
```

Or per service:

```java
@RequestFactory(serviceRoot = "http://localhost:7070/library/")
public interface MoviesService {
  @Path("movies/:movieName")
  @GET
  Movie getMovieByName(@PathParam("movieName") String movieName);
}
```

Dynamic routing is also supported:

```java
DominoRestConfig.getInstance()
    .addDynamicServiceRoot(DynamicServiceRoot
        .pathMatcher(path -> path.startsWith("movies"))
        .serviceRoot(() -> "http://localhost:7070/library/")
    );
```

### Resource root

When using the default service root, the resource root defaults to `service/`. You can change it:

```java
DominoRestConfig.getInstance()
    .setDefaultResourceRootPath("endpoint");
```

## Paths and parameters

- `@Path` defines endpoints. Use `:name` or `{name}` placeholders.
- `@PathParam` fills path placeholders.
- `@QueryParam` adds query parameters.
- `@HeaderParam` adds request headers.
- `@MatrixParam` appends matrix params to the path segment.

Example path + path param:

```java
@RequestFactory
public interface MoviesService {
  @Path("library/movies/{name}")
  @GET
  Movie getMovieByName(@PathParam("name") String movieName);
}
```

### Request body selection

For `POST`, `PUT`, or `PATCH`, the request body is resolved by:

1. A parameter annotated with `@RequestBody`.
2. A parameter type annotated with `@RequestBody`.
3. The last parameter that is not `@QueryParam`, `@HeaderParam`, or `@PathParam`.

## Produces/Consumes and custom mappers

Domino REST defaults to JSON. To use custom formats, define a `RequestWriter`/`ResponseReader` and bind it via
`@Writer`/`@Reader` or `CustomMapper`.

```java
public class XmlMovieWriter implements RequestWriter<Movie> {
  @Override
  public String write(Movie request) {
    String movieXml = /* convert to xml */;
    return movieXml;
  }
}
```

```java
@PUT
@Consumes(MediaType.APPLICATION_XML)
@Writer(MovieXmlWriter.class)
void updateMovie(@BeanParam @RequestBody Movie movie);
```

## Request meta-data

Each request exposes a `RequestMeta` instance for inspection in callbacks or interceptors (method, URL, params,
consumes/produces, and more).

## Interceptors, failure handling, and retries

Global interceptors can adjust requests or responses:

```java
DominoRestConfig.getInstance()
    .addResponseInterceptor(new ResponseInterceptor() {
      @Override
      public void onBeforeFailedCallback(ServerRequest serverRequest, FailedResponseBean failedResponse) {
        if (failedResponse.getStatusCode() == 401) {
          serverRequest.skipFailHandler();
        }
      }
    });
```

Override the default fail handler:

```java
DominoRestConfig.getInstance()
    .setDefaultFailHandler(failedResponse -> { });
```

Timeouts and retries are configured per request:

```java
@Retries(timeout = 3000, maxRetries = 5)
void updateMovie(@BeanParam @RequestBody Movie movie);
```

## Credentials and custom URL

Use `@WithCredentials` or per-request `setWithCredentials(true)` to send cookies/credentials on cross-site requests.

For HATEOAS or dynamic URLs, use `setUrl` on the request to override all mapping:

```java
MoviesServiceFactory.INSTANCE
    .updateMovie(movie)
    .setUrl("http://localhost:6060/movies")
    .send();
```

## Multipart forms

Send `multipart/form-data` by using `@FormParam` or grouping with `@Multipart`:

```java
@POST
@Path("upload")
@Consumes(MediaType.MULTIPART_FORM_DATA)
void textMultipart(@FormParam("id") String id, @FormParam("file") byte[] fileContent);
```

## Generic requests and raw responses

When you do not want a typed interface, use `RestRequestBuilder`, or return `jakarta.ws.rs.core.Response` to inspect
status, headers, and body.

## Resource locators and interface inheritance

Resource locators allow splitting sub-resources while keeping full path composition:

```java
@RequestFactory
@Path("library")
public interface LibraryResource {
  @Path("movies")
  MoviesResource movies();
}
```

A request factory interface may extend other interfaces (even external ones) to generate a single client.

## Date formatting

For `Date` parameters on `@QueryParam`, `@PathParam`, or `@HeaderParam`, use `@DateFormat` to control formatting.

## Build

```bash
mvn -DskipTests install
```

## More documentation

- [Domino-rest getting started](https://dominokit.com/solutions/domino-rest/v2/docs/getting-started)
