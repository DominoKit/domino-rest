package org.dominokit.rest.shared;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.client.GWTTestCase;
import org.dominokit.rest.shared.RestfulRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class RestfulRequestTest extends GWTTestCase {

    protected static final Logger LOGGER = Logger.getLogger(RestfulRequestTest.class.getCanonicalName());
    private static final String GET = "GET";
    public static final String REQUEST_QUERY_STRING = "request-query-string";

    private RestfulRequest restfulRequest;

    @Override
    protected void gwtSetUp() {
        restfulRequest = RestfulRequest.get(getUri());
    }

    private RestfulRequest create(String uri) {
        return create(uri, GET);
    }

    private RestfulRequest create(String uri, String method) {
        return RestfulRequest.request(uri, method);
    }

    public void testCreateWithInvalidUri() {
        try {
            RestfulRequest.request(null, null);
            fail("URI must not be null");
        } catch (IllegalArgumentException e) {
        }

        try {
            RestfulRequest.request("   ", null);
            fail("URI must not be empty");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testValidUri() {
        assertEquals(getUri(), restfulRequest.getUri());
    }

    public void testAddParameterWithNullValue() {
        restfulRequest.addQueryParam("key", null);
        assertEquals(getUri() + "?key=null", restfulRequest.getUri());
    }

    public void testSetQueryParamater() {
        restfulRequest.addQueryParam("key", "value1");
        restfulRequest.setQueryParam("key", "value2");
        assertEquals(getUri() + "?key=value2", restfulRequest.getUri());
    }

    public void testGetUriWithParameters() {
        RestfulRequest restfulRequest = create(getUri() + "?key1=value1").addQueryParam("key2", "value2");
        assertEquals(getUri() + "?key1=value1&key2=value2", restfulRequest.getUri());
    }

    public void testCreateWithUriThatHasParam_shouldParseParameters() {
        assertEquals("key1=value1&key2=value2", create(getUri() + "?key1=value1&key2=value2").getQuery());
    }

    public void testGetPath() {
        assertEquals(getUri(), create(getUri() + "?key1=value1&key2=value2").getPath());
    }

    public void testGetPathEndsWithSlash_slashShouldBeTrimmed() {
        assertEquals(getUri(), create(getUri() + "/").getPath());
    }

    public void testCreateRequestWithUriAndQueryParam() {
        RestfulRequest baseRestfulRequest = create(getUri()).addQueryString("key1=value1&key2=value2");
        assertEquals(getUri(), baseRestfulRequest.getPath());
        assertEquals("key1=value1&key2=value2", baseRestfulRequest.getQuery());
    }

    public void testCreateWithInvalidMethod_shouldThrowException() {
        try {
            create(getUri(), null);
            fail("Method must be null");
        } catch (IllegalArgumentException e) {
        }

        try {
            create(getUri(), "   ");
            fail("Method must be empty");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testPutHeader() {
        restfulRequest.putHeader("key", "value");
        assertEquals("value", restfulRequest.getHeaders().get("key"));
    }

    public void testSendWithNoBody() {
        RestfulRequest.get(getUri()).onSuccess(response -> {
            assertEquals("test content", response.getBodyAsString());
            finish();
        }).onError(throwable -> fail()).send();
        wait(200);
    }

    public void testSendJson() {
        RestfulRequest jsonRequest = RestfulRequest.post(getUri());
        jsonRequest.onSuccess(response -> {
            assertEquals("test content with body [" + expectedJson() + "]", response.getBodyAsString());
            assertEquals("application/json", response.getHeader("request-header-Content-Type"));
            finish();
        }).onError(throwable -> fail()).sendJson(json());
        wait(200);
    }

    public void testSendForm() {
        Map<String, String> formData = new HashMap<>();
        formData.put("key", "value");
        RestfulRequest formRequest = RestfulRequest.post(getUri());
        formRequest.onSuccess(response -> {
            assertEquals("test content with body [key=value]", response.getBodyAsString());
            assertEquals("application/x-www-form-urlencoded", response.getHeader("request-header-Content-Type"));
            finish();
        }).onError(throwable -> fail()).sendForm(formData);
        wait(200);
    }

    public void testSendString() {
        RestfulRequest formRequest = RestfulRequest.post(getUri());
        formRequest.onSuccess(response -> {
            assertEquals("test content with body [string value]", response.getBodyAsString());
            finish();
        }).onError(throwable -> fail()).send("string value");
        wait(200);
    }

    public void testSendWithQueryParameters() {
        restfulRequest.addQueryParam("key", "value").onSuccess(response -> {
            assertEquals("key=value", response.getHeader(REQUEST_QUERY_STRING));
            finish();
        }).onError(throwable -> fail()).send();
        wait(200);
    }

    public void testAddMultipleQueryParameter() {
        restfulRequest.addQueryParam("key1", "value1").addQueryParam("key2", "value2").onSuccess(response -> {
            assertEquals("key1=value1&key2=value2", response.getHeader(REQUEST_QUERY_STRING));
            finish();
        }).onError(throwable -> fail()).send();
        wait(200);
    }

    public void testAddQueryParameters() {
        restfulRequest.addQueryParams("key", Arrays.asList("value1", "value2")).onSuccess(response -> {
            assertEquals("key=value1&key=value2", response.getHeader(REQUEST_QUERY_STRING));
            finish();
        }).onError(throwable -> fail()).send();
        wait(200);
    }

    @DoNotRunWith(Platform.HtmlUnitBug)
    public void testRequestTimeout() {
        restfulRequest.addQueryParam("timeout", "1000").timeout(500).onSuccess(response -> {
            fail("request should be timed out");
        }).onError(throwable -> finish()).send();
        wait(1000);
    }

    @DoNotRunWith(Platform.HtmlUnitBug)
    public void testRequestNotTimeout() {
        restfulRequest.addQueryParam("timeout", "100").timeout(500).onSuccess(response -> {
            finish();
        }).onError(throwable -> fail("Request should not be timed out")).send();
        wait(1000);
    }

    @DoNotRunWith(Platform.HtmlUnitBug)
    public void testNegativeRequestTimeout() {
        restfulRequest.addQueryParam("timeout", "100").timeout(-100).onSuccess(response -> {
            finish();
        }).onError(throwable -> fail("Request should not be timed out")).send();
        wait(1000);
    }

    protected abstract String getUri();

    /**
     * Put the current test in asynchronous mode. If the test method completes
     * normally, this test will not immediately succeed. Instead, a <i>delay
     * period</i> begins. During the delay period, if the {@link #finish()} method
     * is called before the delay period expires,
     * the test will succeed, otherwise the test will fail</li>
     * <p>
     * This method is typically used to test event driven functionality.
     * </p>
     */
    protected abstract void wait(int millis);

    protected abstract void finish();

    protected abstract String json();

    protected abstract String expectedJson();
}
