// Initial template generated from S.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class S {

  private S() { }



  public static void seed_in(LongPtr p0) {
     throw new UnimplementedGnuApiMethod("seed_in");
  }

  public static void seed_out(LongPtr p0) {
     throw new UnimplementedGnuApiMethod("seed_out");
  }

  public static double unif_rand() {
    return Random.unif_rand();
  }

  public static double norm_rand() {
    return Random.norm_rand();
  }
}
