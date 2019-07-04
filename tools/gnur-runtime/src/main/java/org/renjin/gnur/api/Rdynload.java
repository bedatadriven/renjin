/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from Rdynload.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;
import org.renjin.sexp.SEXP;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class Rdynload {

  private static final Map<String,MethodHandle> CALL_MAP = new HashMap<>();

  private Rdynload() { }


  public static int R_registerRoutines (DllInfo info,
                                        Ptr croutines,
                                        Ptr callRoutines,
                                        Ptr fortranRoutines,
                                        Ptr externalRoutines) {

    addTo(info, DllSymbol.Convention.C, croutines);
    addTo(info, DllSymbol.Convention.CALL, callRoutines);
    addTo(info, DllSymbol.Convention.FORTRAN, fortranRoutines);
    addTo(info, DllSymbol.Convention.EXTERNAL, externalRoutines);

    return 0;
  }


  private static void addTo(DllInfo library, DllSymbol.Convention convention, Ptr methods) {

    if(!methods.isNull()) {
      for(int i=0; ; i++) {
        MethodDef2 def = new MethodDef2(methods.pointerPlus(i * MethodDef2.BYTES));
        if (def.fun == null) {
          break;
        }
        library.register(new DllSymbol(def.getName(), transformMethodHandle(def.fun, convention), convention, true));
      }
    }
  }

  private static MethodHandle transformMethodHandle(MethodHandle target, DllSymbol.Convention convention) {
    if(convention == DllSymbol.Convention.EXTERNAL) {
      if(target.type().parameterCount() == 0) {
        // Allow this method handle to except an additional argument that
        // is dropped before calling the function
        return MethodHandles.dropArguments(target, 0, SEXP.class);
      }
    }
    // Otherwise no transformation required
    return target;
  }

  public static boolean R_useDynamicSymbols(DllInfo info, boolean value) {
    return info.setUseDynamicSymbols(value);
  }

  public static boolean R_forceSymbols(DllInfo info, boolean value) {
    return info.forceSymbols(value);
  }

//
//   DllInfo* R_getDllInfo (const char *name)
//
//   DllInfo* R_getEmbeddingDllInfo (void)
//
//   DL_FUNC R_FindSymbol (char const *, char const *, R_RegisteredNativeSymbol *symbol)
//

  @Deprecated
  public static void R_RegisterCCallable (BytePtr packageName, BytePtr name, Object method) {
    R_RegisterCCallable(packageName, name, (MethodHandle)method);
  }

  public static void R_RegisterCCallable (BytePtr packageName, BytePtr name, MethodHandle method) {
    // We assume this is thread save given if multiple sessions are Registering or Getting
    // a method the packages/functions stay the same and the order of processing is irrelevant
    String key = packageName.nullTerminatedString() + "." + name.nullTerminatedString();
    CALL_MAP.put(key, method);
  }

  public static MethodHandle R_GetCCallable (BytePtr packageName, BytePtr name) {
    String key = packageName.nullTerminatedString() + "." + name.nullTerminatedString();
    return CALL_MAP.get(key);
  }
}
