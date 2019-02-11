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
package org.renjin.sexp;

import org.junit.Test;
import org.renjin.repackaged.guava.primitives.UnsignedBytes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RawVectorTest {

  @Test
  public void asCharacter() {
    RawVector vector = new RawVector(UnsignedBytes.checkedCast(1));
    assertThat(vector.getElementAsString(0), equalTo("01"));

    vector = new RawVector(UnsignedBytes.checkedCast(128));
    assertThat(vector.getElementAsString(0), equalTo("80"));


  }
  
}