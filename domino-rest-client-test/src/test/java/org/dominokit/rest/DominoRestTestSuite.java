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
package org.dominokit.rest;

import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.dominokit.rest.client.ServicePathGwtTest;
import org.dominokit.rest.client.UrlFormatterGwtTest;
import org.dominokit.rest.client.UrlSplitUtilGwtTest;

public class DominoRestTestSuite extends GWTTestSuite {
  public static Test suite() {
    TestSuite suite = new TestSuite("Tests for client domino-rest");
    suite.addTestSuite(UrlSplitUtilGwtTest.class);
    suite.addTestSuite(UrlFormatterGwtTest.class);
    suite.addTestSuite(ServicePathGwtTest.class);

    return suite;
  }
}
