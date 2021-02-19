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
//// import io.vertx.core.json.Json;
// import junit.framework.TestCase;
// import org.eclipse.jetty.server.Connector;
// import org.eclipse.jetty.server.LocalConnector;
// import org.eclipse.jetty.server.Server;
// import org.eclipse.jetty.servlet.ServletHolder;
// import org.eclipse.jetty.webapp.WebAppContext;
//
// import java.util.logging.Level;
//
// public class JavaRestfulRequestTest extends RestfulRequestTest {
//
//    private static final int PORT = 18080;
//    private boolean finished = false;
//
//    static {
//        Server server = new Server(PORT);
//        Connector con = new LocalConnector(server);
//        server.addConnector(con);
//        WebAppContext webAppContext = new WebAppContext();
//        webAppContext.setResourceBase("/");
//        webAppContext.setContextPath("/");
//        webAppContext.addServlet(new ServletHolder(new TestServlet()), "/testRequest");
//        server.setHandler(webAppContext);
//        try {
//            server.start();
//            RestfulRequestTest.LOGGER.info("server is started");
//        } catch (Exception e) {
//            RestfulRequestTest.LOGGER.log(Level.SEVERE, "Cannot start test server " +
// e.getLocalizedMessage());
//        }
//    }
//
//    @Override
//    protected void gwtSetUp() {
//        super.gwtSetUp();
//        finished = false;
//    }
//
//    @Override
//    public String getModuleName() {
//        return null;
//    }
//
//    @Override
//    protected String getUri() {
//        return "http://localhost:" + PORT + "/testRequest";
//    }
//
//    @Override
//    protected void wait(int millis) {
//        try {
//            Thread.sleep(millis);
//            if (!finished)
//                TestCase.fail();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void finish() {
//        this.finished = true;
//    }
//
//    @Override
//    protected String json() {
////        return Json.encode(new Message());
//        return null;
//    }
//
//    @Override
//    protected String expectedJson() {
//        return "{\"message\":\"test message\"}";
//    }
//
//    public class Message {
//        public String message = "test message";
//    }
// }
