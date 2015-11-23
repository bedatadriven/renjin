// Initial template generated from Callbacks.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class Callbacks {

  private Callbacks() { }



  public static boolean Rf_removeTaskCallbackByIndex(int id) {
     throw new UnimplementedGnuApiMethod("Rf_removeTaskCallbackByIndex");
  }

  public static boolean Rf_removeTaskCallbackByName(CharPtr name) {
     throw new UnimplementedGnuApiMethod("Rf_removeTaskCallbackByName");
  }

  public static SEXP R_removeTaskCallback(SEXP which) {
     throw new UnimplementedGnuApiMethod("R_removeTaskCallback");
  }

  // R_ToplevelCallbackEl* Rf_addTaskCallback (R_ToplevelCallback cb, void *data, void(*finalizer)(void *), const char *name, int *pos)
}
