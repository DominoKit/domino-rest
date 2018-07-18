package org.dominokit.rest.server;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

import static java.util.Objects.nonNull;

public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setResponseHeaders(req, resp);
        String queryString = req.getQueryString();
        if (nonNull(queryString) && queryString.contains("timeout")) {
            String[] timeoutPair = queryString.split("=");
            try {
                Thread.sleep(Integer.parseInt(timeoutPair[1]));
            } catch (InterruptedException e) {
            }
        }
        resp.getWriter().print("test content");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setResponseHeaders(req, resp);
        String requestBody = IOUtils.toString(req.getInputStream());
        resp.getWriter().print("test content with body [" + requestBody + "]");
    }

    private void setResponseHeaders(HttpServletRequest req, HttpServletResponse resp) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            resp.addHeader("request-header-" + headerName, req.getHeader(headerName));
        }
        resp.setHeader("request-query-string", req.getQueryString());
    }
}
