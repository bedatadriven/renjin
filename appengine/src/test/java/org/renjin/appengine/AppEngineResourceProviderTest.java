/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.appengine;

import org.easymock.EasyMock;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

public class AppEngineResourceProviderTest {

  @Test
  public void testResourceFromJarFile() throws Throwable {

    String resourceName = "org/renjin/sexp/SEXP.class";

    File contextRoot = new File("/home/alex/dev/renjin-examples/appengine-servlet/target/renjin-appengine-servlet-example-1.0-SNAPSHOT");

    URL expectedUrl = new URL("jar:" +
        "file:" +
        "/home/alex/dev/renjin-examples/appengine-servlet/target/renjin-appengine-servlet-example-1.0-SNAPSHOT" +
        "/WEB-INF/lib/renjin-core-0.9.2692.jar!" +
        "/org/renjin/sexp/SEXP.class");

    assumeThat(expectedUrl.toExternalForm(), equalTo(
        "jar:" +
          "file:" +
            "/home/alex/dev/renjin-examples/appengine-servlet/target/renjin-appengine-servlet-example-1.0-SNAPSHOT" +
            "/WEB-INF/lib/renjin-core-0.9.2692.jar!" +
          "/org/renjin/sexp/SEXP.class"));

    ClassLoader classLoader = EasyMock.createMock(ClassLoader.class);
    EasyMock.expect(classLoader.getResource(resourceName)).andReturn(expectedUrl).anyTimes();
    EasyMock.replay(classLoader);

    AppEngineResourceProvider resourceProvider = new AppEngineResourceProvider(classLoader, contextRoot);
    String relativeUri = resourceProvider.findResourceRelativeToContextRoot(resourceName);

    assertThat(relativeUri, equalTo(
        "jar:" +
          "file:" +
            "/WEB-INF/lib/renjin-core-0.9.2692.jar!" +
          "/org/renjin/sexp/SEXP.class"));



  }


//  @Test
//  public void testResourceFromClassesDir() throws Throwable {
//
//    String resourceName = "org/renjin/example/appengine/RenjinServlet.class";
//
//    File contextRoot = new File("/home/alex/dev/renjin-examples/appengine-servlet/target/renjin-appengine-servlet-example-1.0-SNAPSHOT");
//
//    URL expectedUrl = new URL(
//        "jar:" +
//              "file:" +
//                "/home/alex/dev/renjin-examples/appengine-servlet/target/renjin-appengine-servlet-example-1.0-SNAPSHOT" +
//                "/WEB-INF/lib/renjin-appengine-servlet-example-1.0-SNAPSHOT.jar!" +
//            "/org/renjin/example/appengine/RenjinServlet.class");
//
//    ClassLoader classLoader = EasyMock.createMock(ClassLoader.class);
//    EasyMock.expect(classLoader.getResource(resourceName)).andReturn(expectedUrl).anyTimes();
//    EasyMock.replay(classLoader);
//
//    AppEngineResourceProvider resourceProvider = new AppEngineResourceProvider(classLoader, contextRoot);
//    String relativeUri = resourceProvider.findResourceRelativeToContextRoot(resourceName);
//
//    assertThat(relativeUri, equalTo("jar:file:/home/alex/dev/renjin-examples/appengine-servlet/target/renjin-appengine-servlet-example-1.0-SNAPSHOT/WEB-INF/lib/renjin-appengine-servlet-example-1.0-SNAPSHOT.jar!/org/renjin/example/appengine/RenjinServlet.class"
//
//
//
//  }

}