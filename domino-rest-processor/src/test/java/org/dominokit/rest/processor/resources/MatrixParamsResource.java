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
package org.dominokit.rest.processor.resources;

import jakarta.ws.rs.*;
import java.util.List;
import java.util.stream.Collectors;
import org.dominokit.rest.processor.MatrixParamsService;
import org.dominokit.rest.processor.SampleRequest;
import org.dominokit.rest.processor.TestResponse;

/** JAX-RS resource implementation for MatrixParamsService testing. */
@Path("matrix-test")
public class MatrixParamsResource implements MatrixParamsService {

  public TestResponse getBooksByCategory(String category) {
    return TestResponse.make().addMatrixParameter("category", category);
  }

  public TestResponse getBooksByAuthorAndYear(String author, int year) {
    return TestResponse.make()
        .addMatrixParameter("author", author)
        .addMatrixParameter("year", String.valueOf(year));
  }

  public TestResponse getBooksInLibrary(String libraryId, String category) {
    return TestResponse.make()
        .addPathParameter("libraryId", libraryId)
        .addMatrixParameter("category", category);
  }

  public TestResponse getBooksByAuthorPaginated(String author, int page, int size) {
    return TestResponse.make()
        .addQueryParameter("page", String.valueOf(page))
        .addQueryParameter("size", String.valueOf(size))
        .addMatrixParameter("author", author);
  }

  public TestResponse getBooksByGenres(List<String> genres) {
    String genreList =
        genres == null
            ? "[]"
            : "["
                + genres.stream().map(g -> "\"" + g + "\"").collect(Collectors.joining(","))
                + "]";
    return TestResponse.make().addMatrixParameter("genre", genreList);
  }

  public TestResponse getBooksInShelf(String storeId, String shelfId, String condition) {
    return TestResponse.make()
        .addPathParameter("storeId", storeId)
        .addPathParameter("shelfId", shelfId)
        .addMatrixParameter("condition", condition);
  }

  public TestResponse createBook(String format, SampleRequest bookData) {
    return TestResponse.make().addMatrixParameter("format", format);
  }

  public TestResponse filterProducts(double minPrice, double maxPrice, boolean inStock) {
    return TestResponse.make()
        .addMatrixParameter("minPrice", String.valueOf(minPrice))
        .addMatrixParameter("maxPrice", String.valueOf(maxPrice))
        .addMatrixParameter("inStock", String.valueOf(inStock));
  }

  public TestResponse getItemsInCatalog(String catalogId, String tag) {
    return TestResponse.make()
        .addPathParameter("catalogId", catalogId)
        .addMatrixParameter("tag", tag);
  }

  public TestResponse getInventory(
      String warehouseId, String status, boolean includeReserved, String token) {
    return TestResponse.make()
        .addPathParameter("warehouseId", warehouseId)
        .addMatrixParameter("status", status)
        .addQueryParameter("includeReserved", String.valueOf(includeReserved))
        .addHeader("X-Warehouse-Token", token);
  }
}
