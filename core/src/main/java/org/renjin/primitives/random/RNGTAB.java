package org.renjin.primitives.random;

import java.util.Arrays;

public class RNGTAB {

  RNGtype kind;
  N01type nkind;
  String name;

  public RNGTAB(RNGtype kind, N01type nkind, String name) {
    this.kind = kind;
    this.nkind = nkind;
    this.name = name;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(name).append(":  ").append(kind).append("-").append(nkind).append("\n");
    return (buf.toString());
  }
}
