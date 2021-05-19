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
package org.dominokit.rest.model;

import javax.ws.rs.FormParam;

public class SampleMultipartRequest {

  @FormParam("sampleObjectJson")
  private SampleObject sampleObject;

  @FormParam("file")
  private byte[] fileContent;

  @FormParam("size")
  private int size;

  public SampleObject getSampleObject() {
    return sampleObject;
  }

  public void setSampleObject(SampleObject sampleObject) {
    this.sampleObject = sampleObject;
  }

  public byte[] getFileContent() {
    return fileContent;
  }

  public void setFileContent(byte[] fileContent) {
    this.fileContent = fileContent;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
