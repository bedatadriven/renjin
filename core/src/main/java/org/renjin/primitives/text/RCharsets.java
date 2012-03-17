package org.renjin.primitives.text;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;

public final class RCharsets {
  
  public static Charset getByName(String name) {
    if("UTF8".equals(name) || "unknown".equals(name)) {
      return Charsets.UTF_8;
    } else {
      return Charset.forName(name);
    }
  }
}
