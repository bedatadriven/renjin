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

package org.renjin.base;

import com.google.common.collect.Sets;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;
import org.renjin.util.FileSystemUtils;

import java.util.IdentityHashMap;
import java.util.Set;

/**
 *  The {@code Frame} that provides the primitive functions for the
 *  the base environment.
 *
 *  <p>
 *  The base frame is actually SHARED between the base namespace and the base environment:
 *  they are identical except for their parent of course.
 *  
 */
public class BaseFrame implements Frame {

  private final IdentityHashMap<Symbol, SEXP> loaded = new IdentityHashMap<Symbol, SEXP>(1100);

  @Override
  public Set<Symbol> getSymbols() {
    return Sets.union(Primitives.getBuiltinSymbols(), loaded.keySet());
  }

  @Override
  public SEXP getVariable(Symbol name) {
    SEXP value = Primitives.getBuiltin(name);
    if(value != null) {
      return value;
    }
    value = loaded.get(name);
    if(value != null ) {
      return value;
    }
    return Symbol.UNBOUND_VALUE;
  }
  
  @Override
  public Function getFunction(Symbol name) {
    SEXP value = Primitives.getBuiltin(name);
    if(value == null) {
      value = loaded.get(name);
    }
    if(value == null) {
      return null;
    }
    if(value instanceof Promise) {
      value = ((Promise) value).force();
    }
    if(value instanceof Function) {
      return (Function)value;
    } else {
      return null;
    }
  }

  @Override
  public void setVariable(Symbol name, SEXP value) {
    loaded.put(name, value);
  }

  public BaseFrame() {
    installPlatform();
    installMachine();
  }

  private void installPlatform() {
    loaded.put(Symbol.get(".Library"), 
        new StringVector(FileSystemUtils.homeDirectoryInCoreJar() + "/library"));
    loaded.put(Symbol.get(".Library.site"), 
        new StringVector());
    
    loaded.put(Symbol.get(".Platform"), ListVector.newNamedBuilder()
        .add("OS.type", new StringVector(resolveOsName()))
        .add("file.sep", new StringVector("/"))
        .add("GUI", new StringVector("unknown"))
        .add("endian", new StringVector("big"))
        .add("pkgType", new StringVector("source"))
        .add("r_arch", new StringVector(""))
        .add("dynlib.ext", new StringVector(".jar"))
        .build());
  }
  
  

  @Override
  public void remove(Symbol name) {
    loaded.remove(name);
  }

  /**
   * Adds the .Machine list to the base frame,
   * a variable holding information on the numerical characteristics of the machine R is
   * running on, such as the largest double or integer and the machine's precision.
   */
  private void installMachine() {
    // TODO: I'm not sure how these values are used, but
    // I have mostly just copied them from my local R installation
    loaded.put(Symbol.get(".Machine"), ListVector.newNamedBuilder()
        .add("double.eps", new DoubleArrayVector(DoubleVector.EPSILON))
        .add("double.neg.eps", new DoubleArrayVector(1.110223e-16))
        .add("double.xmin",new DoubleArrayVector(2.225074e-308))
        .add("double.xmax", new DoubleArrayVector(1.797693e+308))
        .add("double.base", new IntArrayVector(2))
        .add("double.digits", new IntArrayVector(53))
        .add("double.rounding", new IntArrayVector(5))
        .add("double.guard", new IntArrayVector(0))
        .add("double.ulp.digits", new IntArrayVector(-52))
        .add("double.neg.ulp.digits", new IntArrayVector(-53))
        .add("double.exponent", new IntArrayVector(11))
        .add("double.min.exp", new IntArrayVector(Double.MIN_EXPONENT))
        .add("double.max.exp", new IntArrayVector(Double.MAX_EXPONENT))
        .add("integer.max", new IntArrayVector(Integer.MAX_VALUE))
        .add("sizeof.long", new IntArrayVector(4))
        .add("sizeof.longlong", new IntArrayVector(8))
        .add("sizeof.longdouble", new IntArrayVector(12))
        .add("sizeof.pointer", new IntArrayVector(4))
       .build());

  }

  private String resolveOsName() {
    return java.lang.System.getProperty("os.name").contains("windows") ? "windows" : "unix";
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("The base frame cannot be cleared");
    
  }

  @Override
  public boolean isMissingArgument(Symbol name) {
    return false;
  }
}
