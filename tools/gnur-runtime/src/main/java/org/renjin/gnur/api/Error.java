// Initial template generated from Error.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;

@SuppressWarnings("unused")
public final class Error {

  private Error() { }



  // void Rf_error (const char *,...)

  public static void UNIMPLEMENTED(CharPtr p0) {
     throw new UnimplementedGnuApiMethod("UNIMPLEMENTED");
  }

  public static void WrongArgCount(CharPtr p0) {
     throw new UnimplementedGnuApiMethod("WrongArgCount");
  }

  // void Rf_warning (const char *,...)

  public static void R_ShowMessage(CharPtr s) {
     throw new UnimplementedGnuApiMethod("R_ShowMessage");
  }
}
