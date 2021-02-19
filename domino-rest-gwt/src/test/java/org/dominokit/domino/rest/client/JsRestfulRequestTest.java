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
// package org.dominokit.domino.rest.client;
//
// import com.google.gwt.core.client.GWT;
// import jsinterop.annotations.JsMethod;
// import jsinterop.annotations.JsPackage;
// import jsinterop.annotations.JsType;
// import org.dominokit.domino.rest.RestfulRequestTest;
//
// public class JsRestfulRequestTest extends RestfulRequestTest {
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
// }
