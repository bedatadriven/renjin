package r.lang.primitive.binding;

import r.lang.*;

public class AtomicExps {
  public static Class elementClassOf(Class<? extends AtomicExp> atomicClass) {
    if(atomicClass == LogicalExp.class) {
      return Logical.class;
    } else if(atomicClass == IntExp.class) {
      return Integer.TYPE;
    } else if(atomicClass == DoubleExp.class) {
      return Double.TYPE;
    } else if(atomicClass == StringExp.class) {
      return String.class;
    } else {
      throw new IllegalArgumentException();
    }
  }
}
