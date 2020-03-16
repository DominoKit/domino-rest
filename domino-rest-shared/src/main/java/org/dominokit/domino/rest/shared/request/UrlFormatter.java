package org.dominokit.domino.rest.shared.request;

import org.dominokit.domino.history.StateHistoryToken;
import org.dominokit.domino.rest.shared.request.exception.PathParameterMissingException;
import org.dominokit.domino.rest.shared.request.exception.QueryParameterMissingException;
import org.gwtproject.regexp.shared.MatchResult;
import org.gwtproject.regexp.shared.RegExp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public class UrlFormatter<R> {

    private final Map<String, String> queryParameters;
    private final Map<String, String> pathParameters;
    private final R requestBean;

    public UrlFormatter(Map<String, String> queryParameters, Map<String, String> pathParameters, R requestBean) {
        this.queryParameters = queryParameters;
        this.pathParameters = pathParameters;
        this.requestBean = requestBean;
    }

    protected String formatUrl(String targetUrl) {
        if (isNull(targetUrl)) {
            throw new IllegalArgumentException("URL cannot be null!.");
        }
        if (targetUrl.trim().isEmpty()) {
            return targetUrl.trim();
        }
        if (!hasExpressions(targetUrl)) {
            return targetUrl.trim();
        }

        String postfix = asTokenString(targetUrl);
        String prefix = targetUrl.replace(postfix, "");

        StateHistoryToken tempToken = new StateHistoryToken(postfix);

        replaceUrlParamsWithArguments(tempToken);

        return prefix + (targetUrl.startsWith("/") ? "/" : "") + tempToken.value();
    }

    private boolean hasExpressions(String url) {
        return (url.contains("{") && url.contains("}")) || url.contains(":");
    }

    private void replaceUrlParamsWithArguments(StateHistoryToken tempToken) {
        replacePaths(tempToken);
        replaceQueryParams(tempToken);
    }

    private void replaceQueryParams(StateHistoryToken tempToken) {
        tempToken.queryParameters()
                .entrySet()
                .stream()
                .filter(entry -> isExpressionToken(entry.getValue()))
                .forEach(entry -> tempToken.replaceParameter(entry.getKey(), entry.getKey(), getQueryValue(entry)));
    }

    private void replacePaths(StateHistoryToken tempToken) {
        new ArrayList<>(tempToken.paths())
                .stream()
                .filter(this::isExpressionToken)
                .forEach(path -> tempToken.replacePath(path, getPathValue(path)));
    }

    private String asTokenString(String url) {
        if (url.contains("http:") || url.contains("https:")) {
            RegExp regExp = RegExp.compile("^((.*:)//([a-z0-9\\-.]+)(|:[0-9]+)/)(.*)$");
            MatchResult matcher = regExp.exec(url);
            boolean matchFound = matcher != null; // equivalent to regExp.test(inputStr);
            if (matchFound) {
                return matcher.getGroup(matcher.getGroupCount() - 1);
            }
        }
        return url;
    }

    private boolean hasPathParameter(String path) {
        String pathName = replaceExpressionMarkers(path);
        return pathParameters.containsKey(pathName);
    }

    private String getPathValue(String path) {
        if (!hasPathParameter(path)) {
            throw new PathParameterMissingException(path);
        }
        String pathName = replaceExpressionMarkers(path);
        return pathParameters.get(pathName);
    }

    private boolean hasQueryParameter(Map.Entry<String, String> entry) {
        String queryName = replaceExpressionMarkers(entry.getValue());
        return queryParameters.containsKey(queryName);
    }

    private String getQueryValue(Map.Entry<String, String> entry) {
        if (!hasQueryParameter(entry)) {
            throw new QueryParameterMissingException(entry.getKey());
        }
        String queryName = replaceExpressionMarkers(entry.getValue());
        return queryParameters.get(queryName);
    }

    private boolean isExpressionToken(String tokenPath) {
        return tokenPath.startsWith(":") || (tokenPath.startsWith("{") && tokenPath.endsWith("}"));
    }

    private String replaceExpressionMarkers(String replace) {
        return replace
                .replace(":", "")
                .replace("{", "")
                .replace("}", "");
    }

}
