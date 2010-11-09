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

package r.compiler;

import org.junit.Before;
import org.junit.Test;
import r.lang.DoubleExp;
import r.lang.PairListExp;
import r.lang.StringExp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JavaSourceWritingVisitorTest {
  private JavaSourceWritingVisitor visitor;

  @Test
  public void list() {
    PairListExp expr = PairListExp.fromArray(new DoubleExp(1, 2, 3), new StringExp("a", "b"));
    expr.accept(visitor);

    assertThat(visitor.getBody(), equalTo("list(c(1, 2, 3), c(\"a\", \"b\"))"));
  }

  @Test
  public void naReal() {
    DoubleExp na = new DoubleExp(DoubleExp.NA);
    na.accept(visitor);

    assertThat(visitor.getBody(), equalTo("c(NA_real_)"));
  }

  @Before
  public void setUp() throws Exception {
    visitor = new JavaSourceWritingVisitor();
  }
}
