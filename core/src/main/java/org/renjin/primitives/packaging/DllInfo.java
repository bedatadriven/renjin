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

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Native;
import org.renjin.repackaged.guava.base.Optional;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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
    this.forceSymbols = true;
    return oldValue;
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
   * GNU R provides a way of executing some code automatically when a object/DLL is either loaded or unloaded.
   * This can be used, for example, to register native routines with R's dynamic symbol mechanism, initialize some data
   * in the native code, or initialize a third party library. On loading a DLL, R will look for a routine within that
   * DLL named R_init_lib where lib is the name of the DLL file with the extension removed.
   *
   */
  private Optional<Method> findInitRoutine() {
    String initName = "R_init_" + libraryName;
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
    return Optional.absent();
  }

  private boolean isPublicStatic(Method method) {
    return Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers());
  }

  public Optional<DllSymbol> lookup(String symbolName) {

    if(forceSymbols) {
      return Optional.absent();
    }

    if(registeredSymbols.containsKey(symbolName)) {
      return Optional.of(registeredSymbols.get(symbolName));
    }

    if(useDynamicSymbols) {
      return lookupWithReflection(symbolName);
    }

    return Optional.absent();
  }

  private Optional<DllSymbol> lookupWithReflection(String symbolName) {
    for(Method method : libraryClass.getMethods()) {
      if(method.getName().equals(symbolName) && isPublicStatic(method)) {
        return Optional.of(new DllSymbol(method));
      }
    }
    return Optional.absent();
  }

}
