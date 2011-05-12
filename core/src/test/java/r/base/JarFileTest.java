/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base;

import org.junit.Test;

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
    String url = "jar:file:/C:/Users/Owner/.m2/repository/com/bedatadriven/renjin/renjin-core/0.1.0-SNAPSHOT/renjin-core-0.1.0-SNAPSHOT.jar!/r/lang/SEXP.class";

    assertThat( System.RHomeFromSEXPClassURL(url), equalTo(
        "jar:file:/C:/Users/Owner/.m2/repository/com/bedatadriven/renjin/renjin-core/0.1.0-SNAPSHOT/renjin-core-0.1.0-SNAPSHOT.jar!/r"));

  }

  @Test
  public void RHomeInDir() {
    String expected = getClass().getResource("/r/lang/SEXP.class").getFile();
    expected = expected.substring(1, expected.length()-16);
    assertThat(System.RHomeFromSEXPClassURL(getClass().getResource("/r/lang/SEXP.class").toString()),
        equalTo(expected));
  }
}
