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

package r.base.match;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import r.EvalTestCase;

public class UniqueTest extends EvalTestCase {

  @Test
  public void atomicVectors() {
    assertThat( eval(".Internal(unique(c(1,3,1,4,4), FALSE, FALSE))"), equalTo( c(1,3,4)) );
  }
  
  @Test
  public void fromLast() {
    assertThat( eval(".Internal(unique(c(1,3,1,4,4), FALSE, TRUE))"), equalTo( c(3,1,4)) );    
  }

  @Test
   public void uniqueInt() {
     assertThat( eval(" .Internal(unique(1L, FALSE, FALSE)) "), CoreMatchers.equalTo(c_i(1)));
   }
  
  @Test
  public void falseIncomparablesIsTreatedAsNull() {
    assertThat( eval(" .Internal(unique(c(0, 1, 0, 0, 0, 0, 0, 0), FALSE, FALSE))"), equalTo(c(0,1)));
  }

}
