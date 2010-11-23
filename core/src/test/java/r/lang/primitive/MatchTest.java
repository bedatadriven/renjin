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

package r.lang.primitive;

import org.junit.Test;
import r.lang.EvalTestCase;
import r.lang.IntExp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatchTest extends EvalTestCase {

  @Test
  public void matchDoubles() {
    assertThat( eval( ".Internal(match(92, c(91,92,93), NA_integer_, NULL ))"), equalTo( c_i(2) ));
    assertThat( eval( ".Internal(match(44, c(91,92,93), NA_integer_, NULL ))"), equalTo( c_i(IntExp.NA) ));
  }

  @Test
  public void dontMatchIncomparables() {
    assertThat( eval( ".Internal(match(92, c(91,92,93), NA_integer_, c(92L) ))"), equalTo( c_i(IntExp.NA) ));
  }

  @Test
  public void matchStrings() {
    assertThat( eval( ".Internal(match( c(1,2), c('z', 'y', '1', '2'), NA_integer_, FALSE)) "), equalTo( c_i(3, 4)));
  }
}
