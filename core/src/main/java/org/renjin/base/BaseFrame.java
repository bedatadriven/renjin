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

import java.io.IOException;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.Set;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrame;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Frame;
import org.renjin.sexp.Function;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.util.FileSystemUtils;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;

/**
 *  The {@code Frame} that provides the primitive functions for the
 *  the base environment as well as the base package functions written
 *  in the R language.
 *
 *  <p>
 *  The base frame is actually SHARED between the base namespace and the base environment:
 *  they are identical except for their place within the environment hierarchy.
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
  public Function getFunction(Context context, Symbol name) {
    SEXP value = Primitives.getBuiltin(name);
    if(value == null) {
      value = loaded.get(name);
    }
    if(value == null) {
      return null;
    }
    if(value instanceof Promise) {
      value = value.force(context);
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
            StringVector.valueOf(FileSystemUtils.homeDirectoryInCoreJar() + "/library"));
    loaded.put(Symbol.get(".Library.site"), 
        new StringArrayVector());
    
    loaded.put(Symbol.get(".Platform"), ListVector.newNamedBuilder()
        .add("OS.type", resolveOsName())
        .add("file.sep", "/")
        .add("GUI", "unknown")
        .add("endian", "big")
        .add("pkgType", "source")
        .add("r_arch", "")
        .add("dynlib.ext", dynlibExt())
        .build());
  }

  protected String dynlibExt() {
    String os = java.lang.System.getProperty("os.name").toLowerCase();
    if(os.contains("win")) {
      return ".dll";
    } else if(os.contains("mac")) {
      return ".dylib";
    } else {
      return ".so";
    }
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
        .add("double.eps", DoubleVector.EPSILON)
        .add("double.neg.eps", 1.110223e-16)
        .add("double.xmin", 2.225074e-308)
        .add("double.xmax", 1.797693e+308)
        .add("double.base", 2)
        .add("double.digits", 53)
        .add("double.rounding", 5)
        .add("double.guard", 0)
        .add("double.ulp.digits", -52)
        .add("double.neg.ulp.digits", -53)
        .add("double.exponent", 11)
        .add("double.min.exp", Double.MIN_EXPONENT)
        .add("double.max.exp", Double.MAX_EXPONENT)
        .add("integer.max", Integer.MAX_VALUE)
        .add("sizeof.long", 4)
        .add("sizeof.longlong", 8)
        .add("sizeof.longdouble", 12)
        .add("sizeof.pointer", 4)
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
  
  public void load(Context context) throws IOException {
    URL baseNamespace = Resources.getResource("org/renjin/baseNamespace");
    LazyLoadFrame frame = new LazyLoadFrame(context, Resources.newInputStreamSupplier(baseNamespace));
    for(Symbol name : frame.getNames()) {
      loaded.put(name, frame.get(name));
    }
   
    // aliases
    addPrimitiveAlias("as.double", "as.numeric");
    addPrimitiveAlias("as.double", "as.real");
    addPrimitiveAlias("is.symbol", "is.name");
    
  }

  private void addPrimitiveAlias(String primitiveName, String alias) {
    loaded.put(Symbol.get(alias), Primitives.getBuiltin(primitiveName));
  }
  
}
