// Initial template generated from Rconnections.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

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
