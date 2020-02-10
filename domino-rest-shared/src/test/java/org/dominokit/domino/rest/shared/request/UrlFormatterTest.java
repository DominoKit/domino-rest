package org.dominokit.domino.rest.shared.request;

import org.dominokit.domino.rest.shared.request.exception.PathParameterMissingException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UrlFormatterTest {

    private UrlFormatter<SampleRequestBean> urlFormatter;

    private Map<String, String> queryParameters = new HashMap<>();
    private Map<String, String> pathParameters =  new HashMap<>();
    private Map<String, String> callArguments =  new HashMap<>();
    private RequestParametersReplacer<SampleRequestBean> requestParametersReplacer;
    private SampleRequestBean requestBean;

    @Before
    public void setUp() throws Exception {
        requestBean = new SampleRequestBean(100, "SampleName");
        requestParametersReplacer= (token, request) -> token.value();
    }

    @Test
    public void when_url_is_null_throw_exception() {
        buildFormatter();
        assertThatThrownBy(() -> urlFormatter.formatUrl(null)).isInstanceOf(IllegalArgumentException.class);
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
    public void when_url_has_path_parameter_and_no_path_parameter_provided_then_should_throw_exception() {
        buildFormatter();
        assertThatThrownBy(() -> urlFormatter.formatUrl("/movies/{name}")).isInstanceOf(PathParameterMissingException.class);
    }

    @Test
    public void when_url_has_path_parameter_and_the_parameter_was_provided_then_should_replace_path_parameter() {
        buildFormatter();
        pathParameters.put("name", "hulk");
        assertThat(urlFormatter.formatUrl("/movies/{name}")).isEqualTo("/movies/hulk");
        assertThat(urlFormatter.formatUrl("/movies/:name")).isEqualTo("/movies/hulk");
    }

    @Test
    public void when_url_has_path_parameter_and_the_parameter_was_provided_as_call_argument_then_should_replace_path_parameter() {
        buildFormatter();
        callArguments.put("name", "hulk");
        assertThat(urlFormatter.formatUrl("{name}")).isEqualTo("hulk");
        assertThat(urlFormatter.formatUrl("/movies/:name")).isEqualTo("/movies/hulk");
    }

    @Test
    public void when_url_has_path_parameter_and_the_parameter_was_provided_as_path_parameter_and_call_argument_then_should_replace_path_parameter_from_pth_parameters() {
        buildFormatter();
        pathParameters.put("name", "hulk");
        callArguments.put("name", "troy");
        assertThat(urlFormatter.formatUrl("/movies/{name}")).isEqualTo("/movies/hulk");
        assertThat(urlFormatter.formatUrl("/movies/:name")).isEqualTo("/movies/hulk");
    }

    private void buildFormatter(){
        urlFormatter = new UrlFormatterBuilder<SampleRequestBean>()
                .setCallArguments(callArguments)
                .setPathParameters(pathParameters)
                .setQueryParameters(queryParameters)
                .setRequestParametersReplacer(requestParametersReplacer)
                .setRequestBean(requestBean)
                .build();
    }
}
