package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;

/**
 * Stubs for gettext() routines
 */
public class GetText {


  public static BytePtr gettext(BytePtr message) {
    return message;
  }

  public static BytePtr dgettext(BytePtr packageName, BytePtr message) {
    return message;
  }
}
