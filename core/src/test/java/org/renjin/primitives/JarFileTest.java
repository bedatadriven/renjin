/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.util.FileSystemUtils;

import java.net.MalformedURLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assure that files can be read from jars.
 * (The most common scenario will be that files and data will be bundled
 * into jars on the classpath)
 */
public class JarFileTest {

  @Test
  public void RHomeInJar() throws MalformedURLException {
    String url = "jar:file:/C:/Users/Owner/.m2/repository/com/bedatadriven/renjin/renjin-core/0.1.0-SNAPSHOT/renjin-core-0.1.0-SNAPSHOT.jar!/org/renjin/sexp/SEXP.class";

    assertThat( FileSystemUtils.embeddedRHomeFromSEXPClassURL(url), equalTo(
        "jar:file:/C:/Users/Owner/.m2/repository/com/bedatadriven/renjin/renjin-core/0.1.0-SNAPSHOT/renjin-core-0.1.0-SNAPSHOT.jar!/org/renjin"));

  }

  @Test
  public void RHomeInFs() throws MalformedURLException {
    String url = "jar:file:/usr/lib/renjin/dependencies/renjin-core-0.1.0-SNAPSHOT.jar!/org/renjin/sexp/SEXP.class";

    assertThat( FileSystemUtils.localRHomeFromSEXPClassURL(url), equalTo(
        "file:/usr/lib/renjin"));

  }
  
  @Test
  public void RHomeInDir() {
    String expected = getClass().getResource("/org/renjin/sexp/SEXP.class").getFile();
    expected = expected.substring(0, expected.length()-16);
    assertThat(FileSystemUtils.embeddedRHomeFromSEXPClassURL(getClass().getResource("/org/renjin/sexp/SEXP.class").toString()),
        equalTo(expected));
  }
}
