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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PairListTest {

  @Test
  public void lang() {
    PairList list = PairList.Node.fromArray(new IntArrayVector(1), new IntArrayVector(2), new IntArrayVector(3));
    assertThat(list.length(), equalTo(3));
  }

  @Test
  public void removeLastNode() {
    PairList.Builder x = new PairList.Builder();
    x.add("a", IntArrayVector.valueOf(1));
    x.add("b", IntArrayVector.valueOf(2));
    x.remove(1);

    assertThat(x.length(), equalTo(1));
  }
}
