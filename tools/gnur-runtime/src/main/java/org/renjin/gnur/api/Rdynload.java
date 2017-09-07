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
// Initial template generated from Rdynload.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.gcc.runtime.RecordPtr;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class Rdynload {

  private static final Map<String,MethodHandle> CALL_MAP = new HashMap<>();

  private Rdynload() { }


  @Deprecated
  public static int R_registerRoutines (DllInfo info,
                                        ObjectPtr<MethodDef> croutines,
                                        ObjectPtr<MethodDef> callRoutines,
                                        ObjectPtr<MethodDef> fortranRoutines,
                                        ObjectPtr<MethodDef> externalRoutines) {

    addTo(info, DllSymbol.Convention.C, croutines);
    addTo(info, DllSymbol.Convention.CALL, callRoutines);
    addTo(info, DllSymbol.Convention.FORTRAN, fortranRoutines);
    addTo(info, DllSymbol.Convention.EXTERNAL, externalRoutines);

    return 0;
  }


  public static int R_registerRoutines (DllInfo info,
                                        RecordPtr<MethodDef> croutines,
                                        RecordPtr<MethodDef> callRoutines,
                                        RecordPtr<MethodDef> fortranRoutines,
                                        RecordPtr<MethodDef> externalRoutines) {

    addTo(info, DllSymbol.Convention.C, croutines);
    addTo(info, DllSymbol.Convention.CALL, callRoutines);
    addTo(info, DllSymbol.Convention.FORTRAN, fortranRoutines);
    addTo(info, DllSymbol.Convention.EXTERNAL, externalRoutines);

    return 0;
  }



  private static void addTo(DllInfo library, DllSymbol.Convention convention, ObjectPtr<MethodDef> methods) {

    if(methods != null && methods.array != null) {
      for(int i=0; ; i++) {
        MethodDef def = methods.get(i);
        if (def.fun == null) {
          break;
        }
        library.register(new DllSymbol(def.getName(), def.fun, convention));
      }
    }
  }

  private static void addTo(DllInfo library, DllSymbol.Convention convention, RecordPtr<MethodDef> methods) {

    if(!methods.isNull()) {
      for(int i=0; ; i++) {
        MethodDef def = methods.get(i);
        if (def.fun == null) {
          break;
        }
        library.register(new DllSymbol(def.getName(), def.fun, convention));
      }
    }
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
