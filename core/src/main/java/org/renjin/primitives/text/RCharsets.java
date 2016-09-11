package org.renjin.primitives.text;

import org.renjin.repackaged.guava.base.Charsets;

import java.nio.charset.Charset;

public final class RCharsets {
  
  public static Charset getByName(String name) {
    if("UTF8".equals(name) || "unknown".equals(name)) {
      return Charsets.UTF_8;
    } else {
      return Charset.forName(name);
    }
  }
}
