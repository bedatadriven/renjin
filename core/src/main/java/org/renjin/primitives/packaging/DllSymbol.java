/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.packaging;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * A symbol registered with a dynamic library
 */
public class DllSymbol {



  public enum Convention {
    C("CRoutine"),
    CALL("CallRoutine"),
    FORTRAN("FortranRoutine"),
    EXTERNAL("ExternalRoutine");
    
    private String className;

    Convention(String className) {
      this.className = className;
    }

    public String getClassName() {
      return className;
    }
  }

  private String name;
  private MethodHandle methodHandle;
  private Convention convention;
  private boolean registered;


  public DllSymbol(String name, MethodHandle methodHandle, Convention convention, boolean registered) {
    this.name = name;
    this.methodHandle = methodHandle;
    this.convention = convention;
    this.registered = registered;
  }

  @Deprecated
  public DllSymbol(String name, MethodHandle methodHandle, Convention convention) {
    this(name, methodHandle, convention, true);
  }

  public DllSymbol(Method method) {
    this.name = method.getName();
    this.registered = false;
    try {
      this.methodHandle = MethodHandles.publicLookup().unreflect(method);
    } catch (IllegalAccessException e) {
      throw new EvalException("Cannot access method '%s': %s", method.getName(), e.getMessage(), e);
    }
  }

  public String getName() {
    return name;
  }

  @Deprecated
  public void setName(String name) {
    this.name = name;
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

  public Convention getConvention() {
    return convention;
  }


  /**
   * @return an R NativeSymbolInfo SEXP object
   */
  public ListVector buildNativeSymbolInfoSexp() {

    ListVector.NamedBuilder symbol = new ListVector.NamedBuilder();
    symbol.add("name", name);
    symbol.add("address", buildAddressSexp());
    symbol.add("numParameters", methodHandle.type().parameterCount());

    if (convention != null){
      symbol.setAttribute(Symbols.CLASS, new StringArrayVector(convention.getClassName(), "NativeSymbolInfo"));
    } else {
      symbol.setAttribute(Symbols.CLASS, new StringArrayVector("NativeSymbolInfo"));
    }
    
    return symbol.build();
  }

  private ExternalPtr<MethodHandle> buildAddressSexp() {
    AttributeMap.Builder attributes = AttributeMap.builder();
    if(registered) {
      attributes.setClass("RegisteredNativeSymbol");
    } else {
      attributes.setClass("NativeSymbol");
    }
    return new ExternalPtr<>(methodHandle, attributes.build());
  }

  /**
   * "Parses" an R NativeSymbolInfo SEXP into a DllSymbol object.
   */
  public static DllSymbol fromSexp(SEXP method) {
    ListVector list = (ListVector) method;
    String name = list.getElementAsString("name");
    ExternalPtr<MethodHandle> address = (ExternalPtr<MethodHandle>) list.get("address");
    Convention convention = conventionFromClass(method);
    boolean registered = address.inherits("RegisteredNativeSymbol");

    return new DllSymbol(name, address.getInstance(), convention, registered);
  }

  public static DllSymbol fromAddressSexp(SEXP method) {
    ExternalPtr<MethodHandle> address = (ExternalPtr<MethodHandle>) method;
    boolean registered = address.inherits("RegisteredNativeSymbol");

    return new DllSymbol("native", address.getInstance(), Convention.C, registered);
  }

  private static Convention conventionFromClass(SEXP method) {
    for (Convention convention : Convention.values()) {
      if(method.inherits(convention.getClassName())) {
        return convention;
      }
    }
    return Convention.C;
  }

}
