/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dominokit.rest.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** A class which contains the parts for a multiform form data request */
public class MultipartForm {

  private final List<TextMultipart> textMultiParts = new ArrayList<>();
  private final List<FileMultipart> fileMultiParts = new ArrayList<>();

  /**
   * Adds a new text part
   *
   * @param name the name of the part
   * @param valueSupplier the supplier for the text value
   * @param contentType the content type of the body
   * @return same instance
   */
  public MultipartForm append(String name, Supplier<String> valueSupplier, String contentType) {
    textMultiParts.add(new TextMultipart(name, valueSupplier, contentType));
    return this;
  }

  /**
   * Adds a new binary part
   *
   * @param name the name of the part
   * @param value the byte array value
   * @param contentType the content type of the body
   * @return same instance
   */
  public MultipartForm append(String name, byte[] value, String contentType) {
    fileMultiParts.add(new FileMultipart(name, value, contentType));
    return this;
  }

  /** @return The text parts */
  public List<TextMultipart> getTextMultiParts() {
    return textMultiParts;
  }

  /** @return the binary parts */
  public List<FileMultipart> getFileMultiParts() {
    return fileMultiParts;
  }

  /** A context for holding a binary part */
  public static class FileMultipart {

    private final String name;
    private final byte[] file;
    private final String contentType;

    private FileMultipart(String name, byte[] file, String contentType) {
      this.name = name;
      this.file = file;
      this.contentType = contentType;
    }

    public String name() {
      return name;
    }

    public byte[] value() {
      return file;
    }

    public String contentType() {
      return contentType;
    }
  }

  /** A context for holding a text part */
  public static class TextMultipart {

    private final String name;
    private final Supplier<String> valueSupplier;
    private final String contentType;

    private TextMultipart(String name, Supplier<String> valueSupplier, String contentType) {
      this.name = name;
      this.valueSupplier = valueSupplier;
      this.contentType = contentType;
    }

    public String name() {
      return name;
    }

    public String value() {
      return valueSupplier.get();
    }

    public String contentType() {
      return contentType;
    }
  }
}
