package javax.ws.rs.container;

import javax.ws.rs.GwtIncompatible;


@GwtIncompatible
public interface TimeoutHandler {
    public void handleTimeout(AsyncResponse asyncResponse);
}
