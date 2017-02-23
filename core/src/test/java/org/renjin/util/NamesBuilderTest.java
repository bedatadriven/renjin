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
package org.renjin.util;

import org.junit.Test;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class NamesBuilderTest {

  @Test
  public void build() {
    NamesBuilder builder = NamesBuilder.withInitialLength(3);
    assertThat(builder.haveNames(), equalTo(false));
    
    builder.set(0, "");
    builder.set(1, "");
    assertThat(builder.haveNames(), equalTo(false));

    builder.add("Foo");
    assertThat(builder.haveNames(), equalTo(true));
  }

  @Test
  public void cloneFrom() {
    IntArrayVector vectorWithoutNames = new IntArrayVector(1, 2);
    NamesBuilder builder = NamesBuilder.clonedFrom(vectorWithoutNames);
    builder.set(2, "c");
    StringVector names = (StringVector) builder.build(3);

    assertThat(names.getElementAsString(0), equalTo(""));
    assertThat(names.getElementAsString(1), equalTo(""));
    assertThat(names.getElementAsString(2), equalTo("c"));


  }
  
}
