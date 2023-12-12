/*******************************************************************
* Copyright (c) 2021 Eclipse Foundation
*
* This specification document is made available under the terms
* of the Eclipse Foundation Specification License v1.0, which is
* available at https://www.eclipse.org/legal/efsl.php.
*******************************************************************/
package jakarta.ws.rs.core;

import jakarta.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import jakarta.ws.rs.GwtIncompatible;

@GwtIncompatible
public interface EntityPart {

    static Builder withName(String partName) {
        return null;
    }

    static Builder withFileName(String partAndFileName) {
        return null;
    }

    String getName();

    Optional<String> getFileName();

    InputStream getContent();

    <T> T getContent(Class<T> type) throws IllegalArgumentException, IllegalStateException, IOException,
        WebApplicationException;

    <T> T getContent(GenericType<T> type) throws IllegalArgumentException, IllegalStateException, IOException,
        WebApplicationException;

    MultivaluedMap<String, String> getHeaders();

    MediaType getMediaType();

    interface Builder {

        Builder mediaType(MediaType mediaType) throws IllegalArgumentException;

        Builder mediaType(String mediaTypeString) throws IllegalArgumentException;

        Builder header(String headerName, String... headerValues) throws IllegalArgumentException;

        Builder headers(MultivaluedMap<String, String> newHeaders) throws IllegalArgumentException;

        Builder fileName(String fileName) throws IllegalArgumentException;

        Builder content(InputStream content) throws IllegalArgumentException;

        default Builder content(String fileName, InputStream content) throws IllegalArgumentException {
            return this.fileName(fileName).content(content);
        }

        <T> Builder content(T content, Class<? extends T> type) throws IllegalArgumentException;

        default Builder content(Object content) throws IllegalArgumentException {
            return this.content(content, content.getClass());
        }

        <T> Builder content(T content, GenericType<T> type) throws IllegalArgumentException;

        EntityPart build() throws IllegalStateException, IOException, WebApplicationException;
    }
}
