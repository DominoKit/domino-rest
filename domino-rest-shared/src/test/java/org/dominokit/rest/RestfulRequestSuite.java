package org.dominokit.rest;

import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import org.dominokit.rest.client.JsRestfulRequestTest;
import org.dominokit.rest.server.JavaRestfulRequestTest;

public class RestfulRequestSuite {
    public static Test suite() {
        GWTTestSuite suite = new GWTTestSuite("All restful request tests");
        suite.addTestSuite(JavaRestfulRequestTest.class);
        suite.addTestSuite(JsRestfulRequestTest.class);
        return suite;
    }
}
