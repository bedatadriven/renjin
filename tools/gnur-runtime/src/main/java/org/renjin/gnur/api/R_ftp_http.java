// Initial template generated from R-ftp-http.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;
import org.renjin.gcc.runtime.*;

@SuppressWarnings("unused")
public final class R_ftp_http {

  private R_ftp_http() { }



  public static Ptr R_HTTPOpen(CharPtr url) {
     throw new UnimplementedGnuApiMethod("R_HTTPOpen");
  }

  public static int R_HTTPRead(Ptr ctx, CharPtr dest, int len) {
     throw new UnimplementedGnuApiMethod("R_HTTPRead");
  }

  public static void R_HTTPClose(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("R_HTTPClose");
  }

  public static Ptr R_FTPOpen(CharPtr url) {
     throw new UnimplementedGnuApiMethod("R_FTPOpen");
  }

  public static int R_FTPRead(Ptr ctx, CharPtr dest, int len) {
     throw new UnimplementedGnuApiMethod("R_FTPRead");
  }

  public static void R_FTPClose(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("R_FTPClose");
  }

  // void* RxmlNanoHTTPOpen (const char *URL, char **contentType, const char *headers, int cacheOK)

  public static int RxmlNanoHTTPRead(Ptr ctx, Ptr dest, int len) {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPRead");
  }

  public static void RxmlNanoHTTPClose(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPClose");
  }

  public static int RxmlNanoHTTPReturnCode(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPReturnCode");
  }

  public static CharPtr RxmlNanoHTTPStatusMsg(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPStatusMsg");
  }

  // DLsize_t RxmlNanoHTTPContentLength (void *ctx)

  public static CharPtr RxmlNanoHTTPContentType(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPContentType");
  }

  public static void RxmlNanoHTTPTimeout(int delay) {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPTimeout");
  }

  public static Ptr RxmlNanoFTPOpen(CharPtr URL) {
     throw new UnimplementedGnuApiMethod("RxmlNanoFTPOpen");
  }

  public static int RxmlNanoFTPRead(Ptr ctx, Ptr dest, int len) {
     throw new UnimplementedGnuApiMethod("RxmlNanoFTPRead");
  }

  public static int RxmlNanoFTPClose(Ptr ctx) {
     throw new UnimplementedGnuApiMethod("RxmlNanoFTPClose");
  }

  public static void RxmlNanoFTPTimeout(int delay) {
     throw new UnimplementedGnuApiMethod("RxmlNanoFTPTimeout");
  }

  // DLsize_t RxmlNanoFTPContentLength (void *ctx)

  // void RxmlMessage (int level, const char *format,...)

  public static void RxmlNanoFTPCleanup() {
     throw new UnimplementedGnuApiMethod("RxmlNanoFTPCleanup");
  }

  public static void RxmlNanoHTTPCleanup() {
     throw new UnimplementedGnuApiMethod("RxmlNanoHTTPCleanup");
  }
}
