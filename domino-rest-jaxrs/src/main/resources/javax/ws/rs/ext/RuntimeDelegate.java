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


package javax.ws.rs.ext;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;

public abstract class RuntimeDelegate {

  private static volatile RuntimeDelegate cachedDelegate;

  
  protected RuntimeDelegate() {}

  
  public static RuntimeDelegate getInstance() {
    return new RuntimeDelegate() {
      @Override
      public UriBuilder createUriBuilder() {
        return null;
      }

      @Override
      public VariantListBuilder createVariantListBuilder() {
        return null;
      }

      @Override
      public <T> T createEndpoint(Application application, Class<T> endpointType)
          throws IllegalArgumentException, UnsupportedOperationException {
        return null;
      }

      @Override
      public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
          throws IllegalArgumentException {
        return new HeaderDelegate<T>() {
          @Override
          public T fromString(String value) {
            return null;
          }

          @Override
          public String toString(T value) {
            return String.valueOf(value);
          }
        };
      }

      @Override
      public Link.Builder createLinkBuilder() {
        return null;
      }
    };
  }

  
  public static void setInstance(final RuntimeDelegate rd) {
    RuntimeDelegate.cachedDelegate = rd;
  }

  
  public abstract UriBuilder createUriBuilder();

  
  public abstract VariantListBuilder createVariantListBuilder();

  
  public abstract <T> T createEndpoint(Application application, Class<T> endpointType)
      throws IllegalArgumentException, UnsupportedOperationException;

  
  public abstract <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
      throws IllegalArgumentException;

  
  public static interface HeaderDelegate<T> {

    
    public T fromString(String value);

    
    public String toString(T value);
  }

  
  public abstract Link.Builder createLinkBuilder();
}
