/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.rest.processor;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

/**
 * Test service interface for JAX-RS Matrix Parameters support. Matrix parameters are path segment
 * parameters that appear after a semicolon. Example: /resources/books;author=smith;year=2020
 */
@RequestFactory
@Path("/matrix-test")
public interface MatrixParamsService {

  /**
   * Test single matrix parameter on a path segment. Expected URL:
   * /matrix-test/books;category={category}
   */
  @GET
  @Path("books")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBooksByCategory(@MatrixParam("category") String category);

  /**
   * Test multiple matrix parameters on a single path segment. Expected URL:
   * /matrix-test/books;author={author};year={year}
   */
  @GET
  @Path("books/byauther")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBooksByAuthorAndYear(
      @MatrixParam("author") String author, @MatrixParam("year") int year);

  /**
   * Test matrix parameters combined with path parameters. Expected URL:
   * /matrix-test/library/{libraryId}/books;category={category}
   */
  @GET
  @Path("library/{libraryId}/books")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBooksInLibrary(
      @PathParam("libraryId") String libraryId, @MatrixParam("category") String category);

  /**
   * Test matrix parameters combined with query parameters. Expected URL:
   * /matrix-test/books;author={author}?page={page}&size={size}
   */
  @GET
  @Path("books/pages")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBooksByAuthorPaginated(
      @MatrixParam("author") String author,
      @QueryParam("page") int page,
      @QueryParam("size") int size);

  /**
   * Test matrix parameters with multiple values (list). Expected URL:
   * /matrix-test/books;genre={genre1};genre={genre2};genre={genre3}
   */
  @GET
  @Path("books/genres")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBooksByGenres(@MatrixParam("genre") List<String> genres);

  /**
   * Test matrix parameters on nested paths. Expected URL:
   * /matrix-test/store/{storeId}/shelf/{shelfId}/books;condition={condition}
   */
  @GET
  @Path("store/{storeId}/shelf/{shelfId}/books")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getBooksInShelf(
      @PathParam("storeId") String storeId,
      @PathParam("shelfId") String shelfId,
      @MatrixParam("condition") String condition);

  /** Test POST with matrix parameters. Expected URL: /matrix-test/books;format={format} */
  @POST
  @Path("books")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse createBook(@MatrixParam("format") String format, SampleRequest bookData);

  /**
   * Test matrix parameters with different data types. Expected URL:
   * /matrix-test/products;minPrice={minPrice};maxPrice={maxPrice};inStock={inStock}
   */
  @GET
  @Path("products")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse filterProducts(
      @MatrixParam("minPrice") double minPrice,
      @MatrixParam("maxPrice") double maxPrice,
      @MatrixParam("inStock") boolean inStock);

  /**
   * Test matrix parameters on alternative path syntax with colon. Expected URL:
   * /matrix-test/catalog/:catalogId/items;tag={tag}
   */
  @GET
  @Path("catalog/:catalogId/items")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getItemsInCatalog(
      @PathParam("catalogId") String catalogId, @MatrixParam("tag") String tag);

  /**
   * Test complex scenario: path params, matrix params, query params, and headers. Expected URL:
   * /matrix-test/warehouse/{warehouseId}/inventory;status={status}?includeReserved={includeReserved}
   */
  @GET
  @Path("warehouse/{warehouseId}/inventory")
  @Produces(MediaType.APPLICATION_JSON)
  TestResponse getInventory(
      @PathParam("warehouseId") String warehouseId,
      @MatrixParam("status") String status,
      @QueryParam("includeReserved") boolean includeReserved,
      @HeaderParam("X-Warehouse-Token") String token);
}
