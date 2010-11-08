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

import org.junit.Before;
import org.junit.Test;
import r.lang.DoubleExp;
import r.lang.GlobalContext;
import r.lang.StringExp;

public class AssignmentTest {
  private GlobalContext context;

  @Test
  public void stringTarget() throws Exception {

    Evaluation.assign(context.getGlobalEnvironment(), 
        new StringExp("a"), new DoubleExp(1));
  }

  @Test
  public void symbolTarget() throws Exception {

    Evaluation.assign(context.getGlobalEnvironment(),
        context.getSymbolTable().install("x"), new DoubleExp(1) );

  }

  @Before
  public void setUp() throws Exception {
    context = new GlobalContext();
  }
}
