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

package r.lang;

import java.util.HashMap;
import java.util.Set;

public class HashFrame implements Frame{
  private HashMap<Symbol, SEXP> values = new HashMap<Symbol, SEXP>();

  @Override
  public Set<Symbol> getSymbols() {
    return values.keySet();
  }

  @Override
  public SEXP getVariable(Symbol name) {
    SEXP value = values.get(name);
    return value == null ? Symbol.UNBOUND_VALUE : value;
  }

  @Override
  public SEXP getInternal(Symbol name) {
    return Symbol.UNBOUND_VALUE;
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    values.put(name, value);
  }

  @Override
  public void clear() {
    values.clear();
  }
}
