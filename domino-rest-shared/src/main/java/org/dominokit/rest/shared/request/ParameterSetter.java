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
package org.dominokit.rest.shared.request;

import static java.util.Objects.isNull;

import java.util.Collection;
import java.util.Date;
import java.util.function.Supplier;

/** A utility class to set query parameters for a request */
public class ParameterSetter {
  /**
   * General query parameters setter
   *
   * @param request {@link ServerRequest} the target request for which we add the query parameter
   * @param name String name of the query parameter
   * @param valueSupplier {@link Supplier} for the query parameter value
   * @param <T> The generic type of the query parameter value
   */
  public static <T> void setQueryParameter(
      ServerRequest<?, ?> request, String name, Supplier<T> valueSupplier) {
    if (isNull(valueSupplier) || isNull(valueSupplier.get())) {
      request.getNullQueryParamStrategy().setNullValue(request, name);
    } else {
      request.addQueryParameter(name, String.valueOf(valueSupplier.get()));
    }
  }

  /**
   * General query parameters setter
   *
   * @param request {@link ServerRequest} the target request for which we add the query parameter
   * @param name String name of the query parameter
   * @param valueSupplier {@link Supplier} for the query parameter value
   * @param <T> The generic type of the query parameter value
   */
  public static <T extends Collection<?>> void setCollectionQueryParameter(
      ServerRequest<?, ?> request, String name, Supplier<T> valueSupplier) {
    if (isNull(valueSupplier) || isNull(valueSupplier.get()) || valueSupplier.get().size() < 1) {
      request.getNullQueryParamStrategy().setNullValue(request, name);
    } else {
      valueSupplier
          .get()
          .forEach(value -> ParameterSetter.setQueryParameter(request, name, () -> value));
    }
  }

  /**
   * Date query parameter setter that formats the date using the {@link
   * RestConfig#getDateParamFormatter()}
   *
   * @param request {@link ServerRequest} the target request for which we add the query parameter
   * @param name String name of the query parameter
   * @param valueSupplier a {@link Date} value {@link Supplier}
   * @param pattern String date format pattern
   */
  public static void setDateQueryParameter(
      ServerRequest<?, ?> request, String name, Supplier<Date> valueSupplier, String pattern) {
    if (isNull(valueSupplier) || isNull(valueSupplier.get())) {
      request.getNullQueryParamStrategy().setNullValue(request, name);
    } else {
      request.addQueryParameter(
          name,
          DominoRestContext.make()
              .getConfig()
              .getDateParamFormatter()
              .format(valueSupplier.get(), pattern));
    }
  }

  /**
   * Date query parameter setter that formats the date using the {@link
   * RestConfig#getDateParamFormatter()}
   *
   * @param request {@link ServerRequest} the target request for which we add the query parameter
   * @param name String name of the query parameter
   * @param valueSupplier a {@link Date} collection value {@link Supplier}
   * @param pattern String date format pattern
   */
  public static void setDateCollectionQueryParameter(
      ServerRequest<?, ?> request,
      String name,
      Supplier<? extends Collection<Date>> valueSupplier,
      String pattern) {
    if (isNull(valueSupplier) || isNull(valueSupplier.get()) || valueSupplier.get().size() < 1) {
      request.getNullQueryParamStrategy().setNullValue(request, name);
    } else {
      valueSupplier
          .get()
          .forEach(
              value -> ParameterSetter.setDateQueryParameter(request, name, () -> value, pattern));
    }
  }

  /**
   * Sets general path parameters
   *
   * @param request {@link ServerRequest} the target request for which we add the path parameter
   * @param name String name of the path parameter
   * @param valueSupplier {@link Supplier} for the path parameter value
   * @param <T> The generic type of the path parameter value
   */
  public static <T> void setPathParameter(
      ServerRequest<?, ?> request, String name, Supplier<T> valueSupplier) {
    request.setPathParameter(name, request.emptyOrStringValue(valueSupplier));
  }

  /**
   * Sets a date path parameter
   *
   * @param request {@link ServerRequest} the target request for which we add the path parameter
   * @param name String name of the path parameter
   * @param valueSupplier a {@link Date} value {@link Supplier}
   * @param pattern String date format pattern
   */
  public static void setPathParameter(
      ServerRequest<?, ?> request, String name, Supplier<Date> valueSupplier, String pattern) {
    request.setPathParameter(name, request.formatDate(valueSupplier, pattern));
  }
}
