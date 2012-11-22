package org.renjin.gcc;

import java.io.File;

public class CallingConventions {

  public static CallingConvention fromFile(File file) {
    if(file.getName().endsWith(".f")) {
      return new F77CallingConvention();
    } else {
      return new CallingConvention();
    }
  }
  
}
