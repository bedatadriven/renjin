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
package org.renjin.compiler.ir;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.compiler.ir.TypeSet.*;

public class TypeSetTest {

  @Test
  public void widestTest() {
    assertThat(TypeSet.toString(widestVectorType(DOUBLE, ANY_VECTOR)), equalTo("double|character|complex|list"));
    assertThat(TypeSet.toString(widestVectorType(DOUBLE, INT)), equalTo("double"));
    assertThat(TypeSet.toString(widestVectorType(LIST, ANY_VECTOR)), equalTo("list"));
    assertThat(TypeSet.toString(widestVectorType(INT | DOUBLE, INT | DOUBLE)), equalTo("int|double"));
  }
}