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
// Initial template generated from Rconnections.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.IntPtr;

@SuppressWarnings("unused")
public final class Rconnections {

  private Rconnections() { }



  // int Rconn_fgetc (Rconnection con)

  // int Rconn_ungetc (int c, Rconnection con)

  // int Rconn_getline (Rconnection con, char *buf, int bufsize)

  // int Rconn_printf (Rconnection con, const char *format,...)

  // Rconnection getConnection (int n)

  // Rconnection getConnection_no_err (int n)

  public static boolean switch_stdout(int icon, int closeOnExit) {
    throw new UnimplementedGnuApiMethod("switch_stdout");
  }

  // void Rf_init_con (Rconnection new, const char *description, int enc, const char *const mode)

  // Rconnection R_newurl (const char *description, const char *const mode, int type)

  // Rconnection R_newsock (const char *host, int port, int server, const char *const mode, int timeout)

  // Rconnection in_R_newsock (const char *host, int port, int server, const char *const mode, int timeout)

  // Rconnection R_newunz (const char *description, const char *const mode)

  // int dummy_fgetc (Rconnection con)

  // int dummy_vfprintf (Rconnection con, const char *format, va_list ap)

  public static int getActiveSink(int n) {
    throw new UnimplementedGnuApiMethod("getActiveSink");
  }

  // void Rf_con_pushback (Rconnection con, Rboolean newLine, char *line)

  public static int Rsockselect(int nsock, IntPtr insockfd, IntPtr ready, IntPtr write, double timeout) {
    throw new UnimplementedGnuApiMethod("Rsockselect");
  }

  // void Rf_set_iconv (Rconnection con)
}
