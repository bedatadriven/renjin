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
// Initial template generated from Error.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Stdlib;

import static org.renjin.gnur.api.Utils.R_CheckUserInterrupt;


/**
 * GNU R API methods defined in the "ext/Error.h" header file
 */
@SuppressWarnings("unused")
public final class Error {

  private Error() { }

  @Deprecated
  public static void Rf_warning(BytePtr text) {
    Rf_warning(text, new Object[0]);
  }

  public static void Rf_warning(BytePtr text, Object... formatArgs) {
    Stdlib.printf(text, formatArgs);
  }

  public static void Rf_error(BytePtr text, Object... formatArguments) {
    BytePtr string = new BytePtr(new byte[1024]);
    Stdlib.sprintf(string, text, formatArguments);

    throw new EvalException(string.nullTerminatedString());
  }

  public static void UNIMPLEMENTED(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("UNIMPLEMENTED");
  }

  public static void WrongArgCount(BytePtr p0) {
    throw new UnimplementedGnuApiMethod("WrongArgCount");
  }

  // void Rf_warning (const char *,...)

  public static void R_ShowMessage(BytePtr s) {
    throw new UnimplementedGnuApiMethod("R_ShowMessage");
  }

  public static void rwarn_(BytePtr message, int messageLen) {
    // TODO: hook into warnings
    System.err.println(message.toString(messageLen));
  }

  public static void rexit_(BytePtr message, int messageLen) {
    throw new EvalException(message.toString(messageLen));
  }

  public static void rchkuser_() {
    R_CheckUserInterrupt();
  }

}
