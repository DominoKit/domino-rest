package org.dominokit.domino.rest.shared.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface AsyncRunner {

    Logger LOGGER= LoggerFactory.getLogger(AsyncRunner.class);

    interface AsyncTask{
        void onSuccess();
        default void onFailed(Throwable error){
            LOGGER.error("Failed to run async task : ", error);
        }
    }

    void runAsync(AsyncTask asyncTask);
}
