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

import r.lang.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  The {@code Frame} that provides the primitive functions for the
 *  the base environment.
 *
 *  The singleton instance is immutable and so can be safely shared between
 * multiple threads / contexts.
 */
public class BaseFrame implements Environment.Frame {

  public static final BaseFrame INSTANCE = new BaseFrame();

  private Map<SymbolExp, SEXP> builtins = new HashMap<SymbolExp, SEXP>();
  private Map<SymbolExp, SEXP> internals = new HashMap<SymbolExp, SEXP>();
  private Map<SymbolExp, SEXP> loaded = new HashMap<SymbolExp, SEXP>();

  @Override
  public Set<SymbolExp> getSymbols() {
    return builtins.keySet();
  }

  @Override
  public SEXP getVariable(SymbolExp name) {
    SEXP value = builtins.get(name);
    if(value != null) {
      return value;
    }
    value = loaded.get(name);
    if(value != null ) {
      return value;
    }
    return SymbolExp.UNBOUND_VALUE;
  }

  @Override
  public SEXP getInternal(SymbolExp name) {
    SEXP value = internals.get(name);
    if(value != null) {
      return value;
    }
    return Null.INSTANCE;
  }

  @Override
  public void setVariable(SymbolExp name, SEXP value) {
    loaded.put(name,value);
  }

  private BaseFrame() {
    installPrimitives();
    installPlatform();
  }

  private void installPrimitives() {
    for (FunctionTable.Entry entry : FunctionTable.ENTRIES) {
      SymbolExp symbol = new SymbolExp(entry.name);
      PrimitiveFunction primitive;

      if (entry.eval % 10 != 0) {
        primitive = new BuiltinFunction(entry);
      } else {
        primitive = new SpecialFunction(entry);
      }

      if ((entry.eval % 100) / 10 != 0) {
        internals.put(symbol, primitive);
      } else {
        builtins.put(symbol, primitive);
      }
    }
  }

  private void installPlatform() {
      builtins.put(new SymbolExp(".Platform"), ListVector.newBuilder()
        .add("OS.type", new StringVector(resolveOsName()))
        .add("file.sep", new StringVector("/"))
        .add("GUI", new StringVector("unknown"))
        .add("endian", new StringVector("big"))
        .add("pkgType", new StringVector("source"))
        .add("r_arch", new StringVector(""))
        .build());
  }
  
  private String resolveOsName() {
    return java.lang.System.getProperty("os.name").contains("windows") ? "windows" : "unix";
  }
}
