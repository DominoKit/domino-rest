package org.dominokit.domino.rest.gwt;

import elemental2.dom.DomGlobal;
import elemental2.dom.Location;

public class DefaultServiceRoot {

    public static String get(){
        Location location = DomGlobal.window.location;
        String protocol = location.getProtocol();
        String host = location.getHost();
        return protocol+"//"+host+"/";
    }
}
