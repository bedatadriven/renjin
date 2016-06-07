// Initial template generated from Riconv.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;

@SuppressWarnings("unused")
public final class Riconv {

  private Riconv() { }



  public static Object Riconv_open(CharPtr tocode, CharPtr fromcode) {
    throw new UnimplementedGnuApiMethod("Riconv_open");
  }

  // size_t Riconv (void *cd, const char **inbuf, size_t *inbytesleft, char **outbuf, size_t *outbytesleft)

  public static int Riconv_close(Object cd) {
    throw new UnimplementedGnuApiMethod("Riconv_close");
  }
}
