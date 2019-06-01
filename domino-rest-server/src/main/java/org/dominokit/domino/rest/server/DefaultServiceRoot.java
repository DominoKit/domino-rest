package org.dominokit.domino.rest.server;

import org.dominokit.domino.rest.VertxInstanceProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

public class DefaultServiceRoot {

    public static String get(){
        Iterator<VertxInstanceProvider> iterator = ServiceLoader.load(VertxInstanceProvider.class).iterator();
        VertxInstanceProvider provider;
        if (iterator.hasNext()) {
            provider = iterator.next();
        } else {
            provider = new DefaultProvider();
        }

        String protocol = provider.getProtocol();
        String host = provider.getHost();
        int port = provider.getPort();

        return protocol+"://"+host+":"+port+"/";
    }
}
