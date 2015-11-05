package org.renjin.gnur.api;

import org.renjin.gcc.runtime.DoublePtr;

public class RenjinDebug {

  public static void dump_double(DoublePtr x) {
    System.out.println(x);
  }

}
