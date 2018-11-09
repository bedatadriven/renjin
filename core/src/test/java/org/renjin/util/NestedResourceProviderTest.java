/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NestedResourceProviderTest {


  @Test
  public void normalizeNested() {

    String resourceUrl = "jar:file:/path/to/web.jar!/BOOT-INF/lib/renjin.jar!/org/renjin/sexp/SEXP.class";

    String normalized = ClasspathFileProvider.normalizeNestedJarUris(resourceUrl);

    assertThat(normalized, equalTo("jar:jar:file:/path/to/web.jar!/BOOT-INF/lib/renjin.jar!/org/renjin/sexp/SEXP.class"));
  }

  @Test
  public void normalizeNormal() {

    String resourceUrl = "jar:file:/path/to/web.jar!/org/renjin/sexp/SEXP.class";

    String normalized = ClasspathFileProvider.normalizeNestedJarUris(resourceUrl);

    assertThat(normalized, equalTo(resourceUrl));
  }

  @Test
  public void normalizeNormalNested() {

    String resourceUrl = "jar:jar:file:/path/to/web.jar!/BOOT-INF/lib/renjin.jar!/org/renjin/sexp/SEXP.class";

    String normalized = ClasspathFileProvider.normalizeNestedJarUris(resourceUrl);

    assertThat(normalized, equalTo("jar:jar:file:/path/to/web.jar!/BOOT-INF/lib/renjin.jar!/org/renjin/sexp/SEXP.class"));
  }

}