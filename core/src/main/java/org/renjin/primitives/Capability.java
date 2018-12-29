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
package org.renjin.primitives;

import org.renjin.eval.Session;

import java.util.Optional;

public enum Capability {

  /**
   * is the ‘jpeg’ function operational?
   */
  JPEG(true),

  /**
   * is the ‘png’ function operational?
   */
  PNG(true),

  /**
   * is the ‘tiff’ function operational?
   */
  TIFF(true),

  /**
   * is the ‘tcltk’ package operational?
   */
  TCLTK(false),

  /**
   *  are the ‘X11’ graphics device and the X11-based data editor available?
   */
  X11("X11", false),

  /*
   * is the ‘quartz’ function operational?
   */
  AQUA(false),

  /**
   *  does the internal method for ‘url’ and ‘download.file‘ support ‘http://’ and ‘ftp://’ URLs?
   */
  HTTP_FTP("http/ftp", true),

  /**
   * are ‘make.socket’ and related functions available?
    */
  SOCKETS(true),

  /**
   * is there support for integrating ‘libxml’ with the R event loop?
   */
  LIBXML(false),

  /**
   * are FIFO connections supported?
   */
  FIFO(true),

  /**
   *  is command-line editing available in the current R session?
   *
   *  <p>This is false in non-interactive sessions.
   */
  CLEDIT {
    @Override
    public boolean evaluate(Session session) {
      return session.getSessionController().isCommandLineEditingAvailable();
    }
  },

  /**
   *  is internationalization conversion via ‘iconv’ supported?
   */
  ICONV(true),

  /**
   *  is there Natural Language Support (for message translations)?
   */
  NLS("NLS",false),

  /**
   * is there support for memory profiling?  See ‘tracemem’
   */
  PROFMEM(false),

  /**
   * is there support for the ‘svg’, ‘cairo_pdf’ and ‘cairo_ps’
   *           devices, and for ‘type = "cairo"’ in the ‘X11’, ‘bmp’,
   *           ‘jpeg’, ‘png’, and ‘tiff’ devices?
   */
  CAIRO(false),

  /**
   *  is ICU available for collation?  See the help on Comparison
   *           and ‘icuSetCollate’: it is never used for a C locale.
   */
  ICU("ICU",false),

  /**
   *  does this build use a ‘C’ ‘long double’ type which is
   *           longer than ‘double’?
   */
  LONG_DOUBLE("long.double", false),

  /**
   *  is ‘libcurl’ available in this build?  Used by function
   *           ‘curlGetHeaders’ and optionally by ‘download.file’ and ‘url’.
   *           As from R 3.3.0 always true for Unix-alikes, and true for
   *           CRAN Windows builds.
   */
  LIBCURL(true);

  private final String capabilityName;
  private boolean present;

  Capability(String capabilityName) {
    this.capabilityName = capabilityName;
  }

  Capability() {
    this.capabilityName = name().toLowerCase();
  }

  Capability(boolean present) {
    this.present = present;
    this.capabilityName = name().toLowerCase();
  }

  Capability(String capabilityName, boolean present) {
    this.capabilityName = capabilityName;
    this.present = present;
  }


  public boolean evaluate(Session session) {
    return present;
  }

  public String getCapabilityName() {
    return capabilityName;
  }


  public static Optional<Capability> forName(String capabilityName) {
    for (Capability value : values()) {
      if(value.getCapabilityName().equals(capabilityName)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
