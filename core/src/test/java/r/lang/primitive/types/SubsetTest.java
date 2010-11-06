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
import r.lang.GlobalContext;
import r.lang.ListExp;
import r.lang.SEXP;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.lang.RealExp.c;

public class SubsetTest {

  private GlobalContext context;

  @Before
  public void setUp() {
    context = new GlobalContext();
  }


  @Test
  public void pairListExact() {

    ListExp list = ListExp.buildList().add(c(1)).taggedWith(context.symbol("alligator"))
                                     .add(c(3)).taggedWith(context.symbol("aardvark")).list();

    SEXP result = Subset.index(list, "all");
    assertThat(result, equalTo((SEXP)c(1)));

  }

}
