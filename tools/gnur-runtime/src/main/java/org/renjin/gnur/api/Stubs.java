package org.renjin.gnur.api;

import org.renjin.gcc.runtime.Ptr;
import org.renjin.sexp.SEXP;

public class Stubs {

  public static long lzma_crc64(Ptr str, int strlen, long crc) {
    throw new UnsupportedOperationException("lzma_crc64");
  }

  public static SEXP Rf_GetOptionDeviceAsk() {
    throw new UnsupportedOperationException("Rf_GetOptionDeviceAsk");
  }
}
