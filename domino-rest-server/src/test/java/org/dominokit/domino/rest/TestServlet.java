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
// import org.apache.commons.io.IOUtils;
//
// import javax.servlet.http.HttpServlet;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import java.util.Enumeration;
//
// import static java.util.Objects.nonNull;
//
// public class TestServlet extends HttpServlet {
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        setResponseHeaders(req, resp);
//        String queryString = req.getQueryString();
//        if (nonNull(queryString) && queryString.contains("timeout")) {
//            String[] timeoutPair = queryString.split("=");
//            try {
//                Thread.sleep(Integer.parseInt(timeoutPair[1]));
//            } catch (InterruptedException e) {
//            }
//        }
//        resp.getWriter().print("test content");
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        setResponseHeaders(req, resp);
//        String requestBody = IOUtils.toString(req.getInputStream());
//        resp.getWriter().print("test content with body [" + requestBody + "]");
//    }
//
//    private void setResponseHeaders(HttpServletRequest req, HttpServletResponse resp) {
//        Enumeration<String> headerNames = req.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            resp.addHeader("request-header-" + headerName, req.getHeader(headerName));
//        }
//        resp.setHeader("request-query-string", req.getQueryString());
//    }
// }
