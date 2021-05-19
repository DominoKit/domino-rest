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

public class ByteArrayProvider {

  private byte[] data;
  private String fileName;

  public static ByteArrayProvider of(byte[] data) {
    return new ByteArrayProvider(data, "blob");
  }

  public static ByteArrayProvider of(byte[] data, String fileName) {
    return new ByteArrayProvider(data, fileName);
  }

  public ByteArrayProvider(byte[] data, String fileName) {
    this.data = data;
    this.fileName = fileName;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
