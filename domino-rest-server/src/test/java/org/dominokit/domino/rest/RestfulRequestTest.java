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
// package org.dominokit.domino.rest;
//
// import org.dominokit.domino.rest.shared.RestfulRequest;
// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Test;
//
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.logging.Logger;
//
// public abstract class RestfulRequestTest{
//
//    protected static final Logger LOGGER =
// Logger.getLogger(RestfulRequestTest.class.getCanonicalName());
//    private static final String GET = "GET";
//    public static final String REQUEST_QUERY_STRING = "request-query-string";
//
//    private RestfulRequest restfulRequest;
//
//    @Before
//    protected void gwtSetUp() {
//        RestfullRequestContext.setFactory(new JavaRestfulRequestFactory());
//        restfulRequest = RestfulRequest.get(getUri());
//    }
//
//    private RestfulRequest create(String uri) {
//        return create(uri, GET);
//    }
//
//    private RestfulRequest create(String uri, String method) {
//        return RestfulRequest.request(uri, method);
//    }
//
//    @Test
//    public void testCreateWithInvalidUri() {
//        try {
//            RestfulRequest.request(null, null);
//            Assert.fail("URI must not be null");
//        } catch (IllegalArgumentException e) {
//        }
//
//        try {
//            RestfulRequest.request("   ", null);
//            Assert.fail("URI must not be empty");
//        } catch (IllegalArgumentException e) {
//        }
//    }
//
//    @Test
//    public void testValidUri() {
//        Assert.assertEquals(getUri(), restfulRequest.getUri());
//    }
//
//    @Test
//    public void testAddParameterWithNullValue() {
//        restfulRequest.addQueryParam("key", null);
//        Assert.assertEquals(getUri() + "?key=null", restfulRequest.getUri());
//    }
//
//    @Test
//    public void testSetQueryParamater() {
//        restfulRequest.addQueryParam("key", "value1");
//        restfulRequest.setQueryParam("key", "value2");
//        Assert.assertEquals(getUri() + "?key=value2", restfulRequest.getUri());
//    }
//
//    @Test
//    public void testGetUriWithParameters() {
//        RestfulRequest restfulRequest = create(getUri() + "?key1=value1").addQueryParam("key2",
// "value2");
//        Assert.assertEquals(getUri() + "?key1=value1&key2=value2", restfulRequest.getUri());
//    }
//
//    @Test
//    public void testCreateWithUriThatHasParam_shouldParseParameters() {
//        Assert.assertEquals("key1=value1&key2=value2", create(getUri() +
// "?key1=value1&key2=value2").getQuery());
//    }
//
//    @Test
//    public void testGetPath() {
//        Assert.assertEquals(getUri(), create(getUri() + "?key1=value1&key2=value2").getPath());
//    }
//
//    @Test
//    public void testGetPathEndsWithSlash_slashShouldBeTrimmed() {
//        Assert.assertEquals(getUri(), create(getUri() + "/").getPath());
//    }
//
//    @Test
//    public void testCreateRequestWithUriAndQueryParam() {
//        RestfulRequest baseRestfulRequest =
// create(getUri()).addQueryString("key1=value1&key2=value2");
//        Assert.assertEquals(getUri(), baseRestfulRequest.getPath());
//        Assert.assertEquals("key1=value1&key2=value2", baseRestfulRequest.getQuery());
//    }
//
//    @Test
//    public void testCreateWithInvalidMethod_shouldThrowException() {
//        try {
//            create(getUri(), null);
//            Assert.fail("Method must be null");
//        } catch (IllegalArgumentException e) {
//        }
//
//        try {
//            create(getUri(), "   ");
//            Assert.fail("Method must be empty");
//        } catch (IllegalArgumentException e) {
//        }
//    }
//
//    @Test
//    public void testPutHeader() {
//        restfulRequest.putHeader("key", "value");
//        Assert.assertEquals("value", restfulRequest.getHeaders().get("key"));
//    }
//
//    @Test
//    public void testSendWithNoBody() {
//        RestfulRequest.get(getUri()).onSuccess(response -> {
//            Assert.assertEquals("test content", response.getBodyAsString());
//            finish();
//        }).onError(throwable -> Assert.fail()).send();
//        wait(200);
//    }
//
//    @Test
//    public void testSendJson() {
//        RestfulRequest jsonRequest = RestfulRequest.post(getUri());
//        jsonRequest.onSuccess(response -> {
//            Assert.assertEquals("test content with body [" + expectedJson() + "]",
// response.getBodyAsString());
//            Assert.assertEquals("application/json",
// response.getHeader("request-header-Content-Type"));
//            finish();
//        }).onError(throwable -> Assert.fail()).sendJson(json());
//        wait(200);
//    }
//
//    @Test
//    public void testSendForm() {
//        Map<String, String> formData = new HashMap<>();
//        formData.put("key", "value");
//        RestfulRequest formRequest = RestfulRequest.post(getUri());
//        formRequest.onSuccess(response -> {
//            Assert.assertEquals("test content with body [key=value]", response.getBodyAsString());
//            Assert.assertEquals("application/x-www-form-urlencoded",
// response.getHeader("request-header-Content-Type"));
//            finish();
//        }).onError(throwable -> Assert.fail()).sendForm(formData);
//        wait(200);
//    }
//
//    @Test
//    public void testSendString() {
//        RestfulRequest formRequest = RestfulRequest.post(getUri());
//        formRequest.onSuccess(response -> {
//            Assert.assertEquals("test content with body [string value]",
// response.getBodyAsString());
//            finish();
//        }).onError(throwable -> Assert.fail()).send("string value");
//        wait(200);
//    }
//
//    @Test
//    public void testSendWithQueryParameters() {
//        restfulRequest.addQueryParam("key", "value").onSuccess(response -> {
//            Assert.assertEquals("key=value", response.getHeader(REQUEST_QUERY_STRING));
//            finish();
//        }).onError(throwable -> Assert.fail()).send();
//        wait(200);
//    }
//
//    @Test
//    public void testAddMultipleQueryParameter() {
//        restfulRequest.addQueryParam("key1", "value1").addQueryParam("key2",
// "value2").onSuccess(response -> {
//            Assert.assertEquals("key1=value1&key2=value2",
// response.getHeader(REQUEST_QUERY_STRING));
//            finish();
//        }).onError(throwable -> Assert.fail()).send();
//        wait(200);
//    }
//
//    @Test
//    public void testAddQueryParameters() {
//        restfulRequest.addQueryParams("key", Arrays.asList("value1", "value2")).onSuccess(response
// -> {
//            Assert.assertEquals("key=value1&key=value2",
// response.getHeader(REQUEST_QUERY_STRING));
//            finish();
//        }).onError(throwable -> Assert.fail()).send();
//        wait(200);
//    }
//
////    @DoNotRunWith(Platform.HtmlUnitBug)
////    public void testRequestTimeout() {
////        restfulRequest.addQueryParam("timeout", "1000").timeout(500).onSuccess(response -> {
////            TestCase.fail("request should be timed out");
////        }).onError(throwable -> finish()).send();
////        wait(1000);
////    }
////
////    @DoNotRunWith(Platform.HtmlUnitBug)
////    public void testRequestNotTimeout() {
////        restfulRequest.addQueryParam("timeout", "100").timeout(500).onSuccess(response -> {
////            finish();
////        }).onError(throwable -> TestCase.fail("Request should not be timed out")).send();
////        wait(1000);
////    }
////
////    @DoNotRunWith(Platform.HtmlUnitBug)
////    public void testNegativeRequestTimeout() {
////        restfulRequest.addQueryParam("timeout", "100").timeout(-100).onSuccess(response -> {
////            finish();
////        }).onError(throwable -> TestCase.fail("Request should not be timed out")).send();
////        wait(1000);
////    }
//
//    protected abstract String getUri();
//
//    /**
//     * Put the current test in asynchronous mode. If the test method completes
//     * normally, this test will not immediately succeed. Instead, a <i>delay
//     * period</i> begins. During the delay period, if the {@link #finish()} method
//     * is called before the delay period expires,
//     * the test will succeed, otherwise the test will fail</li>
//     * <p>
//     * This method is typically used to test event driven functionality.
//     * </p>
//     */
//    protected abstract void wait(int millis);
//
//    protected abstract void finish();
//
//    protected abstract String json();
//
//    protected abstract String expectedJson();
// }
