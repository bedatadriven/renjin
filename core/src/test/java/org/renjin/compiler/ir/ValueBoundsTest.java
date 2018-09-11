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
package org.renjin.compiler.ir;

import org.junit.Test;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.StringVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValueBoundsTest {

  @Test
  public void unionClasses() {
    ValueBounds x0 = ValueBounds.constantValue(DoubleVector.valueOf(1, AttributeMap.builder()
        .set("foo", StringVector.valueOf("bar"))
        .build()));

    ValueBounds x = x0.withVaryingValues();
    ValueBounds y = ValueBounds.builder().setTypeSet(TypeSet.DOUBLE).build();

    ValueBounds u = x.union(y);

    assertThat(u.isFlagSet(ValueBounds.MAYBE_OTHER_ATTR), equalTo(true));

  }

}