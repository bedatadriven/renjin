package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;

public class RenjinDebug {

  public static void dump_double(BytePtr varName, double x) {
    System.out.println(varName.nullTerminatedString() + " = " + x);
  }
  
  public static void dump_int(BytePtr varName, int value) {
    System.out.println(varName.nullTerminatedString() + " = " + value);
  }

}
