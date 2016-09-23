/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;

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
  private DllInfo library;
  private MethodHandle methodHandle;
  private Convention convention;

  public DllSymbol(DllInfo library) {
    this.library = library;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DllInfo getLibrary() {
    return library;
  }

  public void setMethodHandle(MethodHandle methodHandle) {
    this.methodHandle = methodHandle;
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

  public void setLibrary(DllInfo library) {
    this.library = library;
  }

  public void setConvention(Convention convention) {
    this.convention = convention;
  }
  

  /**
   * @return an R NativeSymbolInfo SEXP object
   */
  public ListVector createObject() {

    ListVector.NamedBuilder symbol = new ListVector.NamedBuilder();
    symbol.add("name", name);
    symbol.add("address", new ExternalPtr<MethodHandle>(methodHandle,
        AttributeMap.builder().setClass("RegisteredNativeSymbol").build()));

    symbol.add("numParameters", methodHandle.type().parameterCount());



    if (convention!=null){
      symbol.setAttribute(Symbols.CLASS, new StringArrayVector(convention.getClassName(), "NativeSymbolInfo"));
    } else {
      symbol.setAttribute(Symbols.CLASS, new StringArrayVector("NativeSymbolInfo"));
    }
    
    return symbol.build();
  }
}
