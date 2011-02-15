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
import r.lang.IndexUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArrayTest {


  @Test
  public void arrayToVector() {

    assertThat( IndexUtils.arrayIndexToVectorIndex(index(0, 1, 0), dim(2, 3, 4)), equalTo(2) );
    assertThat( IndexUtils.arrayIndexToVectorIndex(index(1, 2, 2), dim(2, 3, 4)), equalTo(17) );
    assertThat( IndexUtils.arrayIndexToVectorIndex(index(6, 1), dim(8, 2)), equalTo(14) );

  }

  @Test
  public void vectorToArray() {
    assertThat( IndexUtils.vectorIndexToArrayIndex(17, dim(2, 3, 4)), equalTo(index(1,2,2)));
  }

  private int[] dim(int... values) {
    return values;
  }

  private int[] index(int... values) {
    return values;
  }


}
