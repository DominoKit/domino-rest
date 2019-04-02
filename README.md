# domino-rest

## Setup

### Maven dependency

```xml
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-rest</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.dominokit</groupId>
  <artifactId>domino-rest</artifactId>
  <version>1.0-SNAPSHOT</version>
  <classifier>sources</classifier>
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
<inherits name="org.dominokit.rest.DominoRest"/>
```

## Sample

```java

// GET
RestfulRequest.get("url")
    .onSuccess(response -> {

    })
    .onError(throwable -> {

    })
    .send();
    
// POST
RestfulRequest.post("url")
    .onSuccess(response -> {

    })
    .onError(throwable -> {

    })
    .sendJson("{\"key\": \"value\"}");
```
