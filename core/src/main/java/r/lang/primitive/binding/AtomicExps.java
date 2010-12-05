package r.lang.primitive.binding;

import r.lang.*;

public class AtomicExps {
  public static Class elementClassOf(Class<? extends AtomicVector> atomicClass) {
    if(atomicClass == LogicalVector.class) {
      return Logical.class;
    } else if(atomicClass == IntVector.class) {
      return Integer.TYPE;
    } else if(atomicClass == DoubleVector.class) {
      return Double.TYPE;
    } else if(atomicClass == StringVector.class) {
      return String.class;
    } else {
      throw new IllegalArgumentException();
    }
  }
}
