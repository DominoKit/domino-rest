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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.dominokit.rest.shared.request.exception.PathParameterMissingException;
import org.junit.Test;

public class UrlFormatterTest {

  private UrlFormatter urlFormatter;

  private final Map<String, String> pathParameters = new HashMap<>();

  @Test
  public void when_url_is_null_throw_exception() {
    buildFormatter();
    assertThatThrownBy(() -> urlFormatter.formatUrl(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void when_url_is_empty_return_empty_string_result() {
    buildFormatter();
    assertThat(urlFormatter.formatUrl("")).isEqualTo("");
    assertThat(urlFormatter.formatUrl("   ")).isEqualTo("");
  }

  @Test
  public void when_url_has_no_expression_return_return_same_url() {
    buildFormatter();
    assertThat(urlFormatter.formatUrl("/movies/hulk")).isEqualTo("/movies/hulk");
    assertThat(urlFormatter.formatUrl("/movies/{hulk{")).isEqualTo("/movies/{hulk{");
    assertThat(urlFormatter.formatUrl("/movies/}hulk}")).isEqualTo("/movies/}hulk}");
  }

  @Test
  public void
      when_url_has_path_parameter_and_no_path_parameter_provided_then_should_throw_exception() {
    buildFormatter();
    assertThatThrownBy(() -> urlFormatter.formatUrl("/movies/{name}"))
        .isInstanceOf(PathParameterMissingException.class);
  }

  @Test
  public void
      when_url_has_path_parameter_and_the_parameter_was_provided_then_should_replace_path_parameter() {
    buildFormatter();
    pathParameters.put("name", "hulk");
    assertThat(urlFormatter.formatUrl("/movies/{name}")).isEqualTo("/movies/hulk");
    assertThat(urlFormatter.formatUrl("/movies/:name")).isEqualTo("/movies/hulk");
  }

  @Test
  public void
      when_url_has_path_parameter_and_the_parameter_was_provided_as_path_parameter_and_call_argument_then_should_replace_path_parameter_from_pth_parameters() {
    buildFormatter();
    pathParameters.put("name", "hulk");
    assertThat(urlFormatter.formatUrl("/movies/{name}")).isEqualTo("/movies/hulk");
    assertThat(urlFormatter.formatUrl("/movies/:name")).isEqualTo("/movies/hulk");
  }

  private void buildFormatter() {
    urlFormatter = new UrlFormatter(pathParameters);
  }
}
