package org.renjin.gcc.jimple;

import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

public class Jimple {

  public static String id(String name) {
    return name.replace('.', '$');
  }


  public static String type(Class<?> type) {
    return type.toString();
  }
}
