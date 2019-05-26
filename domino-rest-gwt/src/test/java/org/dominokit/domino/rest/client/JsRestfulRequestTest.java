//package org.dominokit.domino.rest.client;
//
//import com.google.gwt.core.client.GWT;
//import jsinterop.annotations.JsMethod;
//import jsinterop.annotations.JsPackage;
//import jsinterop.annotations.JsType;
//import org.dominokit.domino.rest.RestfulRequestTest;
//
//public class JsRestfulRequestTest extends RestfulRequestTest {
//
//    @Override
//    public String getModuleName() {
//        return "org.dominokit.rest.DominoRestTest";
//    }
//
//    @Override
//    protected String getUri() {
//        return GWT.getModuleBaseURL() + "testRequest";
//    }
//
//    @Override
//    protected void wait(int millis) {
//        delayTestFinish(millis);
//    }
//
//    @Override
//    protected void finish() {
//        finishTest();
//    }
//
//    @Override
//    protected String json() {
//        Message value = new Message();
//        value.message = "test message";
//        return stringify(value);
//    }
//
//    @Override
//    protected String expectedJson() {
//        return "{\"message\":\"test message\"}";
//    }
//
//    @JsMethod(namespace = "JSON")
//    private static native String stringify(Object value);
//
//    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
//    public static class Message {
//        public String message;
//    }
//}
