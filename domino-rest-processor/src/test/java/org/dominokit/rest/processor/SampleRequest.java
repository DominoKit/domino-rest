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
package org.dominokit.rest.processor;

import java.util.Date;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import org.dominokit.jackson.annotation.JSONMapper;
import org.dominokit.rest.shared.request.service.annotations.DateFormat;
import org.dominokit.rest.shared.request.service.annotations.RequestBody;

@JSONMapper
@RequestBody
public class SampleRequest {

  @PathParam("name")
  private String name;

  private String title;

  @QueryParam("name2")
  public String anotherName;

  @HeaderParam("desc")
  public String description;

  @QueryParam("birth-date")
  @DateFormat("dd-MM-yyyy")
  private Date birthDate;

  private NestedRequestBean accountInfo;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAnotherName() {
    return anotherName;
  }

  public void setAnotherName(String anotherName) {
    this.anotherName = anotherName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public NestedRequestBean getAccountInfo() {
    return accountInfo;
  }

  public void setAccountInfo(NestedRequestBean accountInfo) {
    this.accountInfo = accountInfo;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }
}
