package org.renjin.appl;

import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

public class ExternalRoutines {

  public static void dumpf_(CharPtr ptr, DoublePtr x, IntPtr n, int ldx) {
    System.out.println("----------");
    System.out.println(ptr.asString());
    for(int i=0;i!=n.unwrap();++i) {
      System.out.println(x.array[x.offset+i]);
    }
    System.out.println("----------");

    
  }


  public static void dumpi_(CharPtr ptr, IntPtr x, IntPtr n, int ldx) {
    System.out.println("----------");
    System.out.println(ptr.asString());
    for(int i=0;i!=n.unwrap();++i) {
      System.out.println(x.array[x.offset+i]);
    }
    System.out.println("----------");

  }

  public static void dumpmsg_(CharPtr ptr, int trail) {
    System.out.println(ptr.asString());
  }
  
  public static void dumpl_(CharPtr ptr, boolean value, int what) {
    System.out.println(ptr.asString() + " = " + value);
  }
}
