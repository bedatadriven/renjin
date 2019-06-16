package org.renjin.gnur.api;

import org.renjin.gcc.runtime.Ptr;
import org.renjin.primitives.Native;
import org.renjin.sexp.Logical;
import org.renjin.sexp.SEXP;

public class Stubs {

  public static long lzma_crc64(Ptr str, int strlen, long crc) {
    throw new UnsupportedOperationException("lzma_crc64");
  }

  public static int Rf_GetOptionDeviceAsk() {

//    int ask;
//    ask = asLogical(GetOption1(install("device.ask.default")));
//    if(ask == NA_LOGICAL) {
//      warning(_("invalid value for \"device.ask.default\", using FALSE"));
//      return FALSE;
//    }
//    return ask != 0;
//
    SEXP ask = Native.currentContext().getSession().getOptions().get("device.ask.default");
    Logical logical = ask.asLogical();

    if(logical == Logical.FALSE) {
      return 0;
    } else {
      return 1;
    }
  }
}
