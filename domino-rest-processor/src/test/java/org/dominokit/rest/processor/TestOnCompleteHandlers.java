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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.dominokit.rest.shared.request.service.annotations.RequestFactory;

public class TestOnCompleteHandlers {

  public static void main(String[] args) {

    TestOnCompleteHandlers_UserServiceFactory.INSTANCE
        .get()
        .onSuccess(response -> {})
        .onFailed(failedResponse -> {})
        .onComplete(() -> {})
        .send();
  }

  @RequestFactory
  public interface UserService {

    @Path("/")
    @GET
    User get();
  }

  public static class User {
    private int id;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }
  }
}
