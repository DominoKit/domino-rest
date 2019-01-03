package org.dominokit.rest.server;

import io.vertx.core.json.Json;
import junit.framework.TestCase;
import org.dominokit.rest.shared.RestfulRequestTest;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.LocalConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.logging.Level;

public class JavaRestfulRequestTest extends RestfulRequestTest {

    private static final int PORT = 18080;
    private boolean finished = false;

    static {
        Server server = new Server(PORT);
        Connector con = new LocalConnector(server);
        server.addConnector(con);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setResourceBase("/");
        webAppContext.setContextPath("/");
        webAppContext.addServlet(new ServletHolder(new TestServlet()), "/testRequest");
        server.setHandler(webAppContext);
        try {
            server.start();
            RestfulRequestTest.LOGGER.info("server is started");
        } catch (Exception e) {
            RestfulRequestTest.LOGGER.log(Level.SEVERE, "Cannot start test server " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void gwtSetUp() {
        super.gwtSetUp();
        finished = false;
    }

    @Override
    public String getModuleName() {
        return null;
    }

    @Override
    protected String getUri() {
        return "http://localhost:" + PORT + "/testRequest";
    }

    @Override
    protected void wait(int millis) {
        try {
            Thread.sleep(millis);
            if (!finished)
                TestCase.fail();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finish() {
        this.finished = true;
    }

    @Override
    protected String json() {
        return Json.encode(new Message());
    }

    @Override
    protected String expectedJson() {
        return "{\"message\":\"test message\"}";
    }

    public class Message {
        public String message = "test message";
    }
}
