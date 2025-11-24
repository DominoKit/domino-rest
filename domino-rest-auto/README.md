# domino-rest-auto

Annotation processor that reads an OpenAPI 3.1 document from the classpath (referenced via `@RestAuto` on a `package-info.java`) and generates JAX-RS interfaces annotated with `@RequestFactory` per OpenAPI tag. Method names come from `operationId` and parameters map to JAX-RS `@PathParam`, `@QueryParam`, `@HeaderParam`, or `@CookieParam`. Request bodies become a parameter annotated with `@RequestBody` and `@Consumes`; primary responses drive return types and `@Produces`.

## Planned configuration knobs (to be implemented incrementally)
- Include/exclude tags or paths to narrow generation scope.
- Override generated interface/method naming strategies (e.g., suffix/prefix, path-based names when `operationId` is missing).
- Map component schemas to existing model packages or allow custom type overrides (e.g., primitives vs. domain classes, ref-to-class mapping).
- Choose how `servers`/`serviceRoot` are applied (global default vs. per-interface overrides).
- Control media type selection and fallbacks (prefer JSON, allow multiple `@Produces`/`@Consumes`).
- Toggle generation of documentation (JavaDoc from summaries/descriptions) and validation annotations.
- Support security schemes (inject headers/query params) and global headers.
- Customize request body parameter naming and the handling of optional/nullable parameters.
- Option to emit a single interface for all operations instead of per tag.
