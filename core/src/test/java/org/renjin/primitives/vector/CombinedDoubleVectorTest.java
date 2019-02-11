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
package org.renjin.primitives.vector;


import org.junit.Test;
import org.renjin.primitives.combine.view.CombinedDoubleVector;
import org.renjin.primitives.sequence.DoubleSequence;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class CombinedDoubleVectorTest {

  @Test
  public void test() {
    Vector vectors[] = new Vector[] {
      new DoubleSequence(1, 1, 100),
      new DoubleSequence(98, 2, 100),
      new DoubleArrayVector(5, 4, 3, 2)
    };

    DoubleVector combined = CombinedDoubleVector.combine(vectors, AttributeMap.EMPTY);

    assertThat(combined.length(), equalTo(204));
    assertThat(combined.getElementAsDouble(100), equalTo(vectors[1].getElementAsDouble(0)));
    assertThat(combined.getElementAsDouble(200), equalTo(5d));
    assertThat(combined.getElementAsDouble(203), equalTo(2d));

    // just verify that we can get all the elements without error
    for(int i=0;i!=combined.length();++i) {
      assertThat(combined.getElementAsDouble(i), not(equalTo(0d)));
    }

  }

}
