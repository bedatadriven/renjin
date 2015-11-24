// Initial template generated from Error.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BytePtr;

@SuppressWarnings("unused")
public final class Error {

  private Error() { }
  
  public static void Rf_warning(BytePtr text) {
    // TODO, properly
    System.err.println(text.nullTerminatedString());
  }

  public static void Rf_error(BytePtr text, Object... formatArguments) {
    throw new EvalException(text.nullTerminatedString());
  }

  public static void UNIMPLEMENTED(BytePtr p0) {
     throw new UnimplementedGnuApiMethod("UNIMPLEMENTED");
  }

  public static void WrongArgCount(BytePtr p0) {
     throw new UnimplementedGnuApiMethod("WrongArgCount");
  }

  // void Rf_warning (const char *,...)

  public static void R_ShowMessage(BytePtr s) {
     throw new UnimplementedGnuApiMethod("R_ShowMessage");
  }
  
  public static void rwarn_(BytePtr message, int messageLen) {
    // TODO: hook into warnings
    System.err.println(message.toString(messageLen));
  }
  
}
