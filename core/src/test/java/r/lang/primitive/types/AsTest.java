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

package r.lang.primitive.types;

import org.junit.Before;
import org.junit.Test;
import r.lang.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.ExpMatchers.stringVectorOf;

public class AsTest {
  private As.Character fn;

  @Test
  public void asCharacterFromReal() {
    assertThat(fn.apply(new RealExp(1)), equalTo((SEXP)new StringExp("1")));
  }

  @Test
  public void asCharacterFromManyReals() {
    assertThat(fn.apply(new RealExp(1, 3, 5, 9, 25)), equalTo((SEXP)new StringExp("1", "3", "5", "9", "25")));
  }

  @Test
  public void asCharacterFromInt() {
    assertThat(fn.apply(new IntExp(41)), equalTo((SEXP)new StringExp("41")));
  }

  @Test
  public void asCharacterFromLogical() {
    assertThat(fn.apply(new LogicalExp(true)), stringVectorOf("TRUE"));
  }

  @Test
  public void asCharacterFromListOfReals() {
    assertThat(fn.apply(ListExp.fromArray(new RealExp(1.5), new RealExp(1.6), new RealExp(1.7))),
        equalTo((SEXP)new StringExp("1.5", "1.6", "1.7")));
  }

  @Test
  public void asCharacterFromListOfList() {
    assertThat(fn.apply(ListExp.fromArray(new RealExp(1), ListExp.fromArray(new RealExp(2), new RealExp(3)))),
        equalTo((SEXP)new StringExp("1", "list(2, 3)")));
  }


  @Before
  public void setUp() throws Exception {
    fn = new As.Character();
  }
}
