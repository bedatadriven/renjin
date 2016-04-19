// Initial template generated from Riconv.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.Ptr;

@SuppressWarnings("unused")
public final class Riconv {

  private Riconv() { }



  public static Ptr Riconv_open(CharPtr tocode, CharPtr fromcode) {
    throw new UnimplementedGnuApiMethod("Riconv_open");
  }

  // size_t Riconv (void *cd, const char **inbuf, size_t *inbytesleft, char **outbuf, size_t *outbytesleft)

  public static int Riconv_close(Ptr cd) {
    throw new UnimplementedGnuApiMethod("Riconv_close");
  }
}
