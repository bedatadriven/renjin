// Initial template generated from Memory.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.Ptr;

@SuppressWarnings("unused")
public final class Memory {

  private Memory() {
  }


  public static Ptr vmaxget() {
    throw new UnimplementedGnuApiMethod("vmaxget");
  }

  public static void vmaxset(Ptr p0) {
    throw new UnimplementedGnuApiMethod("vmaxset");
  }

  public static void R_gc() {
    // NOOP
  }

  public static int R_gc_running() {
    return 0;
  }

}
