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
package org.renjin.sexp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.math.complex.Complex;
import org.junit.Test;

public class RecyclingVectorTest {

  @Test
  public void recyclingComplexVector() {
    Complex[] array = new Complex[] { ComplexVector.complex(0, 1), ComplexVector.complex(1, 2) };
    ComplexVector base = new ComplexArrayVector(array);
    ComplexVector recycling = new RecyclingComplexVector(5, base);
    assertThat(recycling.length(), equalTo(5));
    assertThat(recycling.getElementAsComplex(3), equalTo(ComplexVector.complex(1, 2)));
  }

  @Test
  public void recyclingDoubleVector() {
    double[] array = new double[] { 0.1, 1.2 };
    DoubleVector base = new DoubleArrayVector(array);
    DoubleVector recycling = new RecyclingDoubleVector(5, base);
    assertThat(recycling.length(), equalTo(5));
    assertThat(recycling.getElementAsDouble(3), equalTo(1.2));
  }

  @Test
  public void recyclingIntVector() {
    int[] array = new int[] { 0, 1 };
    IntVector base = new IntArrayVector(array);
    IntVector recycling = new RecyclingIntVector(5, base);
    assertThat(recycling.length(), equalTo(5));
    assertThat(recycling.getElementAsInt(3), equalTo(1));
  }

  @Test
  public void recyclingLogicalVector() {
    boolean[] array = new boolean[] { false, true };
    LogicalVector base = new LogicalArrayVector(array);
    LogicalVector recycling = new RecyclingLogicalVector(5, base);
    assertThat(recycling.length(), equalTo(5));
    assertThat(recycling.getElementAsLogical(3), equalTo(LogicalVector.TRUE.asLogical()));
  }

  @Test
  public void recyclingStringVector() {
    String[] array = new String[] { "0.1", "1.2" };
    StringVector base = new StringArrayVector(array);
    StringVector recycling = new RecyclingStringVector(5, base);
    assertThat(recycling.length(), equalTo(5));
    assertThat(recycling.getElementAsString(3), equalTo("1.2"));
  }

}
