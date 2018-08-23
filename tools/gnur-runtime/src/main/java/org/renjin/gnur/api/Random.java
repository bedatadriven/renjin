/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
// Initial template generated from Random.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.nmath.rnorm;
import org.renjin.primitives.Native;

import java.lang.invoke.MethodHandle;

@SuppressWarnings("unused")
public final class Random {

  private Random() { }



  public static void GetRNGstate() {
    // NOOP
  }

  public static void PutRNGstate() {
    // NOOP
  }

  public static double unif_rand() {
    return Native.currentContext().getSession().getRNG().unif_rand();
  }

  public static double norm_rand() {
    MethodHandle runif = Native.currentContext().getSession().getRngMethod();
    return rnorm.rnorm(runif, 0, 1);
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
