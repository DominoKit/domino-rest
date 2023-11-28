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


package javax.ws.rs.core;

import java.io.IOException;


public class NoContentException extends IOException {
  private static final long serialVersionUID = -3082577759787473245L;

  
  public NoContentException(String message) {
    super(message);
  }

  
  public NoContentException(String message, Throwable cause) {
    super(message, cause);
  }

  
  public NoContentException(Throwable cause) {
    super(cause);
  }
}
