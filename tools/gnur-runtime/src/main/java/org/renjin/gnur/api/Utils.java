/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
// Initial template generated from Utils.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.eval.EvalException;
import org.renjin.gcc.annotations.Noop;
import org.renjin.gcc.runtime.*;
import org.renjin.primitives.files.Files;
import org.renjin.repackaged.guava.base.Charsets;

import java.nio.charset.StandardCharsets;


@SuppressWarnings("unused")
public final class Utils {

  private Utils() { }


  public static BytePtr R_ExpandFileName(BytePtr p0) {
    return BytePtr.nullTerminatedString(Files.pathExpand(p0.nullTerminatedString()), StandardCharsets.UTF_8);
  }

  public static void Rf_setIVector(IntPtr p0, int p1, int p2) {
    throw new UnimplementedGnuApiMethod("Rf_setIVector");
  }

  public static void Rf_setRVector(DoublePtr p0, int p1, double p2) {
    throw new UnimplementedGnuApiMethod("Rf_setRVector");
  }

  public static boolean Rf_StringFalse(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_StringFalse");
  }

  public static boolean Rf_StringTrue(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_StringTrue");
  }

  public static boolean Rf_isBlankString(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("Rf_isBlankString");
  }

  @Deprecated
  public static double R_atof(BytePtr str) {
    return Defn.R_atof((Ptr)str);
  }

  @Deprecated
  public static double R_atof(Ptr str) {
    return Defn.R_atof(str);
  }

  // double R_strtod (const char *c, char **end)

  public static BytePtr R_tmpnam(BytePtr prefix, BytePtr tempdir) {
    return BytePtr.nullTerminatedString(
        Files.tempfile(
            Stdlib.nullTerminatedString(prefix),
            Stdlib.nullTerminatedString(tempdir),
            ""), Charsets.UTF_8);
  }

  public static BytePtr R_tmpnam2(BytePtr prefix, BytePtr tempdir, BytePtr fileext) {
    return BytePtr.nullTerminatedString(
        Files.tempfile(
            Stdlib.nullTerminatedString(prefix),
            Stdlib.nullTerminatedString(tempdir),
            Stdlib.nullTerminatedString(fileext)), Charsets.UTF_8);
  }

  public static void R_CheckUserInterrupt() {
    if(Thread.interrupted()) {
      throw new EvalException("Interrupted.");
    }
  }

  @Noop
  public static void R_CheckStack() {
    // Noop: JVM will throw a StackOverflowError for us if need be
  }

  @Noop
  public static void R_CheckStack2(/*size_t*/ int p0) {
    // Noop: JVM will throw a StackOverflowError for us if need be
  }

  public static void R_max_col(DoublePtr matrix, IntPtr nr, IntPtr nc, IntPtr maxes, IntPtr ties_meth) {
    throw new UnimplementedGnuApiMethod("R_max_col");
  }
}
