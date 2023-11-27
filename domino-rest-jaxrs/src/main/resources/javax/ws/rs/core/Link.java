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

import java.util.List;
import java.util.Map;


public abstract class Link {

  public static final String TITLE = "title";

  public static final String REL = "rel";

  public static final String TYPE = "type";

  public abstract UriBuilder getUriBuilder();

  public abstract String getRel();

  public abstract List<String> getRels();

  public abstract String getTitle();

  public abstract String getType();

  public abstract Map<String, String> getParams();

  @Override
  public abstract String toString();

  public interface Builder {
  }

  public static class JaxbLink {

  }

  public static class JaxbAdapter {}
}
