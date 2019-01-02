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
// Initial template generated from R-ftp-http.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;

@SuppressWarnings("unused")
public final class R_ftp_http {

  private R_ftp_http() { }



  public static Object R_HTTPOpen(BytePtr url) {
    throw new UnimplementedGnuApiMethod("R_HTTPOpen");
  }

  public static int R_HTTPRead(Object ctx, BytePtr dest, int len) {
    throw new UnimplementedGnuApiMethod("R_HTTPRead");
  }

  public static void R_HTTPClose(Object ctx) {
    throw new UnimplementedGnuApiMethod("R_HTTPClose");
  }

  public static Object R_FTPOpen(BytePtr url) {
    throw new UnimplementedGnuApiMethod("R_FTPOpen");
  }

  public static int R_FTPRead(Object ctx, BytePtr dest, int len) {
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

  public static BytePtr RxmlNanoHTTPStatusMsg(Object ctx) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPStatusMsg");
  }

  // DLsize_t RxmlNanoHTTPContentLength (void *ctx)

  public static BytePtr RxmlNanoHTTPContentType(Object ctx) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPContentType");
  }

  public static void RxmlNanoHTTPTimeout(int delay) {
    throw new UnimplementedGnuApiMethod("RxmlNanoHTTPTimeout");
  }

  public static Object RxmlNanoFTPOpen(BytePtr URL) {
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
