// Initial template generated from Memory.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.Ptr;

@SuppressWarnings("unused")
public final class Memory {

  private Memory() { }



  public static Ptr vmaxget() {
     throw new UnimplementedGnuApiMethod("vmaxget");
  }

  public static void vmaxset(Ptr p0) {
     throw new UnimplementedGnuApiMethod("vmaxset");
  }

  public static void R_gc() {
     throw new UnimplementedGnuApiMethod("R_gc");
  }

  public static int R_gc_running() {
     throw new UnimplementedGnuApiMethod("R_gc_running");
  }

  public static CharPtr R_alloc(/*size_t*/ int p0, int p1) {
     throw new UnimplementedGnuApiMethod("R_alloc");
  }

  // long double* R_allocLD (size_t nelem)

  // char* S_alloc (long, int)

  // char* S_realloc (char *, long, long, int)
}
