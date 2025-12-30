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
package org.dominokit.rest.shared.request;

/** A provider for byte array data with an optional file name. */
public class ByteArrayProvider {

  private byte[] data;
  private String fileName;

  /**
   * Creates a provider from byte array with default file name.
   *
   * @param data the byte array data
   * @return a new instance
   */
  public static ByteArrayProvider of(byte[] data) {
    return new ByteArrayProvider(data, "blob");
  }

  /**
   * Creates a provider from byte array and file name.
   *
   * @param data the byte array data
   * @param fileName the file name
   * @return a new instance
   */
  public static ByteArrayProvider of(byte[] data, String fileName) {
    return new ByteArrayProvider(data, fileName);
  }

  /**
   * Creates a new instance.
   *
   * @param data the byte array data
   * @param fileName the file name
   */
  public ByteArrayProvider(byte[] data, String fileName) {
    this.data = data;
    this.fileName = fileName;
  }

  /** @return the byte array data */
  public byte[] getData() {
    return data;
  }

  /** @param data the byte array data to set */
  public void setData(byte[] data) {
    this.data = data;
  }

  /** @return the file name */
  public String getFileName() {
    return fileName;
  }

  /** @param fileName the file name to set */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
