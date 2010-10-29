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

import org.junit.Before;
import org.junit.Test;
import r.lang.GlobalContext;
import r.lang.SEXP;
import r.lang.StringExp;
import r.lang.SymbolExp;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class BaseTest {

  private GlobalContext context;

  @Before
  public void setUp() {
    context = new GlobalContext();
    context.loadBasePackage();
  }

  @Test
  public void simplestPossible() {
    SymbolExp lettersSymbol = context.getSymbolTable().install("letters");
    SEXP letters = context.getGlobalEnvironment().findVariable(
        lettersSymbol);
    
    assertThat(letters, instanceOf(StringExp.class));
    assertThat(letters.length(), equalTo(26));
  }

}
