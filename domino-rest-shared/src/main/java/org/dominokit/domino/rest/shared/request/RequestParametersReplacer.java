package org.dominokit.domino.rest.shared.request;


import org.dominokit.domino.history.HistoryToken;

@FunctionalInterface
public interface RequestParametersReplacer<R> {
    String replace(HistoryToken token, R request);
}
