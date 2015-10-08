// Initial template generated from Random.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

@SuppressWarnings("unused")
public final class Random {

  private Random() { }



  public static void GetRNGstate() {
     throw new UnimplementedGnuApiMethod("GetRNGstate");
  }

  public static void PutRNGstate() {
     throw new UnimplementedGnuApiMethod("PutRNGstate");
  }

  public static double unif_rand() {
     throw new UnimplementedGnuApiMethod("unif_rand");
  }

  public static double norm_rand() {
     throw new UnimplementedGnuApiMethod("norm_rand");
  }

  public static double exp_rand() {
     throw new UnimplementedGnuApiMethod("exp_rand");
  }

  public static DoublePtr user_unif_rand() {
     throw new UnimplementedGnuApiMethod("user_unif_rand");
  }

  // void user_unif_init (Int32)

  public static IntPtr user_unif_nseed() {
     throw new UnimplementedGnuApiMethod("user_unif_nseed");
  }

  public static IntPtr user_unif_seedloc() {
     throw new UnimplementedGnuApiMethod("user_unif_seedloc");
  }

  public static DoublePtr user_norm_rand() {
     throw new UnimplementedGnuApiMethod("user_norm_rand");
  }
}
