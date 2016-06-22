// Initial template generated from Memory.h from R 3.2.2
package org.renjin.gnur.api;

@SuppressWarnings("unused")
public final class Memory {

  private Memory() {
  }


  public static Object vmaxget() {
    throw new UnimplementedGnuApiMethod("vmaxget");
  }

  public static void vmaxset(Object p0) {
    throw new UnimplementedGnuApiMethod("vmaxset");
  }

  public static void R_gc() {
    // NOOP
  }

  public static int R_gc_running() {
    return 0;
  }

}
