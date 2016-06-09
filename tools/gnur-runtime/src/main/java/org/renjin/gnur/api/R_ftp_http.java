// Initial template generated from R-ftp-http.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.CharPtr;

@SuppressWarnings("unused")
public final class R_ftp_http {

  private R_ftp_http() { }



  public static Object R_HTTPOpen(CharPtr url) {
    throw new UnimplementedGnuApiMethod("R_HTTPOpen");
  }

  public static int R_HTTPRead(Object ctx, CharPtr dest, int len) {
    throw new UnimplementedGnuApiMethod("R_HTTPRead");
  }

  public static void R_HTTPClose(Object ctx) {
    throw new UnimplementedGnuApiMethod("R_HTTPClose");
  }

  public static Object R_FTPOpen(CharPtr url) {
    throw new UnimplementedGnuApiMethod("R_FTPOpen");
  }

  public static int R_FTPRead(Object ctx, CharPtr dest, int len) {
    throw new UnimplementedGnuApiMethod("R_FTPRead");
  }

  public static void R_FTPClose(Object ctx) {
    throw new UnimplementedGnuApiMethod("R_FTPClose");
  }

  // void* RxmlNanoHTTPOpen (const char *URL, char **contentType, const char *headers, int cacheOK)

  public static int RxmlNanoHTTPRead(Object ctx, Object dest, int len) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPRead");
  }

  public static void RxmlNanoHTTPClose(Object ctx) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPClose");
  }

  public static int RxmlNanoHTTPReturnCode(Object ctx) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPReturnCode");
  }

  public static CharPtr RxmlNanoHTTPStatusMsg(Object ctx) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPStatusMsg");
  }

  // DLsize_t RxmlNanoHTTPContentLength (void *ctx)

  public static CharPtr RxmlNanoHTTPContentType(Object ctx) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPContentType");
  }

  public static void RxmlNanoHTTPTimeout(int delay) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPTimeout");
  }

  public static Object RxmlNanoFTPOpen(CharPtr URL) {
    throw new UnimplementedGnuApiMethod("RxmlNanoFTPOpen");
  }

  public static int RxmlNanoFTPRead(Object ctx, Object dest, int len) {
    throw new UnimplementedGnuApiMethod("RxmlNanoFTPRead");
  }

  public static int RxmlNanoFTPClose(Object ctx) {
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
