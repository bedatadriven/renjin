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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Native;
import org.renjin.repackaged.guava.base.Predicate;
import org.renjin.sexp.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A dynamically loaded "native" library.
 *
 * <p>GNU R provides a comprehensive mechanism for dealing with dynamically loaded libraries
 * compiled from C, C++ and Fortran code.</p>
 *
 * <p>Renjin's tool chain will compile these sources to a set of JVM classes, but we still need to
 * support the bookkeeping that packages expect.</p>
 */
public class DllInfo {

  private final String libraryName;
  private final Class libraryClass;

  /**
   * Index of symbols that have been explicitly registered during the R_init_mylib() call.
   */
  private final Map<String, DllSymbol> registeredSymbols = new HashMap<>();

  /**
   * If false, the DLL will not not be searched for entry points specified by character strings so
   * .C etc calls will only find registered symbol
   */
  private boolean useDynamicSymbols = true;

  /**
   * If true, functions from this DLL cannot be looked up by name at all.
   */
  private boolean forceSymbols = false;

  public DllInfo(String libraryName, Class clazz) {
    this.libraryName = libraryName;
    this.libraryClass = clazz;
  }

  public String getLibraryName() {
    return libraryName;
  }

  public void register(DllSymbol symbol) {
    registeredSymbols.put(symbol.getName(), symbol);
  }

  /**
   * If true, lookup with consider all defined functions in the library.
   *
   * If false, lookup will consider <strong>only</strong> functions explicitly registered.
   */
  public boolean setUseDynamicSymbols(boolean use) {
    boolean oldValue = this.useDynamicSymbols;
    this.useDynamicSymbols = use;
    return oldValue;
  }

  /**
   * if true, functions from this package can only be invoked via the DllSymbol object and
   * <strong>cannot</strong> looked up by name.
   */
  public boolean forceSymbols(boolean value) {
    boolean oldValue = this.forceSymbols;
    this.forceSymbols = value;
    return oldValue;
  }

  public Optional<DllSymbol> getRegisteredSymbol(String name) {
    return Optional.ofNullable(registeredSymbols.get(name));
  }


  public Optional<DllSymbol> getSymbol(String name) {
    DllSymbol symbol = registeredSymbols.get(name);
    if(symbol != null) {
      return Optional.of(symbol);
    }

    if(useDynamicSymbols) {
      return lookupWithReflection(DllSymbol.Convention.C, name);
    }
    return Optional.empty();
  }

  public Iterable<DllSymbol> getRegisteredSymbols() {
    return registeredSymbols.values();
  }


  public void initialize(Context context) {


    // R provides a way of executing some code automatically when a object/DLL is either loaded or unloaded.
    // This can be used, for example, to register native routines with R’s dynamic symbol mechanism, initialize
    // some data in the native code, or initialize a third party library. On loading a DLL, R will look for a
    // routine within that DLL named R_init_lib where lib is the name of the DLL file with the extension removed.

    Optional<Method> initMethod = findInitRoutine();
    if(initMethod.isPresent()) {
      Context previousContext = Native.CURRENT_CONTEXT.get();
      Native.CURRENT_CONTEXT.set(context);
      try {
        if(initMethod.get().getParameterTypes().length == 0) {
          initMethod.get().invoke(null);
        } else {
          initMethod.get().invoke(null, this);
        }

      } catch (InvocationTargetException | IllegalAccessException e) {
        throw new EvalException("Exception initializing compiled GNU R library " + libraryClass, e.getCause());
      } finally {
        Native.CURRENT_CONTEXT.set(previousContext);
      }
    }
  }

  /**
   * Locates a dynamic library's initialization function, if one exists.
   *
   */
  private Optional<Method> findInitRoutine() {
    String initName = "R_init_" + sanitizeLibraryName(libraryName);
    Class[] expectedParameterTypes = new Class[] { DllInfo.class };

    for (Method method : libraryClass.getMethods()) {
      if(method.getName().equals(initName)) {
        if(method.getParameterTypes().length != 0 &&
            !Arrays.equals(method.getParameterTypes(), expectedParameterTypes)) {
          throw new EvalException(String.format("%s.%s has invalid signature: %s. Expected %s(DllInfo info)",
              libraryClass.getName(),
              initName,
              method.toString(),
              initName));
        }
        return Optional.of(method);
      }
    }
    return Optional.empty();
  }

  private String sanitizeLibraryName(String libraryName) {
    return libraryName.replace('.', '_');
  }

  private boolean isPublicStatic(Method method) {
    return Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers());
  }

  public Optional<DllSymbol> lookup(DllSymbol.Convention convention, String symbolName) {

    if(forceSymbols) {
      return Optional.empty();
    }

    Optional<DllSymbol> registeredSymbol = lookupRegisteredSymbol(convention, symbolName);
    if(registeredSymbol.isPresent()) {
      return registeredSymbol;
    }

    if(useDynamicSymbols) {
      return lookupWithReflection(convention, symbolName);
    }

    return Optional.empty();
  }

  private Optional<DllSymbol> lookupRegisteredSymbol(DllSymbol.Convention convention, String symbolName) {

    // When looking up methods using the registration table, calls via ".Fortran"
    // lowercase the name, but do *not* add a trailing '_'
    if(convention == DllSymbol.Convention.FORTRAN) {
      symbolName = symbolName.toLowerCase();
    }

    return Optional.ofNullable(registeredSymbols.get(symbolName));
  }

  private Optional<DllSymbol> lookupWithReflection(DllSymbol.Convention convention, String symbolName) {

    // When using "dynamic lookup", GNU R fully mangles Fortran
    // symbols, applying both lower-casing and a trailing '_'
    if(convention == DllSymbol.Convention.FORTRAN) {
      symbolName = symbolName.toLowerCase() + "_";
    }

    for(Method method : libraryClass.getMethods()) {
      if(method.getName().equals(symbolName) && isPublicStatic(method)) {
        return Optional.of(new DllSymbol(method));
      }
    }
    return Optional.empty();
  }

  /**
   * Creates a "DLLInfo"  object with details of this library.
   */
  public SEXP buildDllInfoSexp() {
    ListVector.NamedBuilder object = ListVector.newNamedBuilder();
    object.setAttribute(Symbols.CLASS, StringVector.valueOf("DLLInfo"));
    object.add("name", libraryName);
    object.add("path", libraryClass.getName());
    object.add("dynamicLookup", useDynamicSymbols);
    object.add("info", new ExternalPtr<>(this));
    return object.build();
  }

  /**
   * Creates a "DLLRegisteredRoutines" object that lists all the registered elements
   */
  public ListVector buildRegisteredRoutinesSexp() {

    ListVector.NamedBuilder object = new ListVector.NamedBuilder();
    object.setAttribute(Symbols.CLASS, StringVector.valueOf("DLLRegisteredRoutines"));
    object.add(".C", buildNativeRoutineList(DllSymbol.Convention.C));
    object.add(".Call", buildNativeRoutineList(DllSymbol.Convention.CALL));
    object.add(".Fortran", buildNativeRoutineList(DllSymbol.Convention.FORTRAN));
    object.add(".External", buildNativeRoutineList(DllSymbol.Convention.EXTERNAL));
    return object.build();
  }

  private ListVector buildNativeRoutineList(DllSymbol.Convention convention) {
    ListVector.NamedBuilder object = new ListVector.NamedBuilder();
    object.setAttribute(Symbols.CLASS, StringVector.valueOf("NativeRoutineList"));

    for (DllSymbol symbol : registeredSymbols.values()) {
      if(symbol.getConvention() == convention) {
        object.add(symbol.getName(), symbol.buildNativeSymbolInfoSexp());
      }
    }
    return object.build();
  }

  public boolean isLoaded(String name, Predicate<DllSymbol> predicate) {
    if(registeredSymbols.containsKey(name)) {
      DllSymbol symbol = registeredSymbols.get(name);
      if(predicate.apply(symbol)) {
        return true;
      }
    }
    return false;

  }
}
