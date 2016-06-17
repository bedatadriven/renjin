// Initial template generated from Rdynload.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

import static org.renjin.gnur.api.CCallablesRegister.callMap;

@SuppressWarnings("unused")
public final class Rdynload {

  private Rdynload() { }


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

  private static void addTo(DllInfo library, DllSymbol.Convention convention, ObjectPtr<MethodDef> methods) {

    if(methods != null && methods.array != null) {
      for(int i=0; ; i++) {
        MethodDef def = methods.get(i);
        if (def.fun == null) {
          break;
        }
        DllSymbol symbol = new DllSymbol(library);
        symbol.setMethodHandle(def.fun);
        symbol.setConvention(convention);
        symbol.setName(def.getName());
        library.addSymbol(symbol);
      }
    }
  }

  public static boolean R_useDynamicSymbols(DllInfo info, boolean value) {
    // unclear what this function does
    return true;
  }

  public static boolean R_forceSymbols(DllInfo info, boolean value) {
    return true;
  }

//
//   DllInfo* R_getDllInfo (const char *name)
//
//   DllInfo* R_getEmbeddingDllInfo (void)
//
//   DL_FUNC R_FindSymbol (char const *, char const *, R_RegisteredNativeSymbol *symbol)
//

  public static void R_RegisterCCallable (BytePtr packageName, BytePtr name, MethodHandle method) {
    String key = packageName.nullTerminatedString() + "." + name.nullTerminatedString();
    CCallablesRegister register = CCallablesRegister.getInstance();
    callMap.put(key, method);
  }

  public static MethodHandle R_GetCCallable (BytePtr packageName, BytePtr name) {
    String key = packageName.nullTerminatedString() + "." + name.nullTerminatedString();
    return callMap.get(key);
  }
}
