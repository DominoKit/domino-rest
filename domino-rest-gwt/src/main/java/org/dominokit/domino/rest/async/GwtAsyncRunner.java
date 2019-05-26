package org.dominokit.domino.rest.async;

import com.google.gwt.core.client.Scheduler;
import org.dominokit.domino.rest.shared.request.AsyncRunner;

public class GwtAsyncRunner implements AsyncRunner {
    @Override
    public void runAsync(AsyncTask asyncTask) {
        Scheduler.get()
                .scheduleDeferred(() -> {
                    try {
                        asyncTask.onSuccess();
                    } catch (Throwable reason) {
                        asyncTask.onFailed(reason);
                    }
                });
    }
}
