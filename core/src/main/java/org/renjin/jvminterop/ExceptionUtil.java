package org.renjin.jvminterop;

import org.renjin.sexp.SEXP;

class ExceptionUtil {

  static String toString(Iterable<SEXP> args) {
    StringBuilder list = new StringBuilder();
    for(SEXP arg : args) {
      if(arg == null) {
        break;
      }
      if(list.length() > 0) {
        list.append(", ");
      }
      list.append(arg.getTypeName());
    }
    return list.toString();
  }

  static String overloadListToString(Iterable<?> overloads) {
    StringBuilder sb = new StringBuilder();
    for(Object overload : overloads) {
      sb.append("\n\t").append(overload.toString());
    }
    return sb.toString();
  }

  
}
