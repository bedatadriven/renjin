/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.io.scan;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringVector;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ListScannerTest extends EvalTestCase {


  @Test
  public void simpleTest() {

    String table = "a,b,c\ne,foo,gee\nh,i,j";
    ByteBuffer buffer = ByteBuffer.wrap(table.getBytes(Charsets.UTF_8));

    AtomicReader readers[] = new AtomicReader[] { new StringReader(), new StringReader(), new StringReader() };
    ListScanner scanner = new ListScanner(readers, (byte)',', (byte)0);
    scanner.process(buffer, true);

    ListVector list = scanner.build();
    assertThat(list.length(), equalTo(3));

    StringVector column1 = (StringVector) list.getElementAsSEXP(0);
    StringVector column2 = (StringVector) list.getElementAsSEXP(1);
    StringVector column3 = (StringVector) list.getElementAsSEXP(2);

    assertThat(column1, equalTo(c("a", "e", "h")));
    assertThat(column2, equalTo(c("b", "foo", "i")));
    assertThat(column3, equalTo(c("c", "gee", "j")));


  }

}