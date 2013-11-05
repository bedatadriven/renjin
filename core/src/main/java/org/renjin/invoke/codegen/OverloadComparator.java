package org.renjin.invoke.codegen;

import org.apache.commons.math.complex.Complex;
import org.renjin.invoke.codegen.scalars.ScalarTypes;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.sexp.*;

import java.util.Comparator;


class OverloadComparator implements Comparator<JvmMethod> {
  private static final int ATOMIC_VECTOR_GROUP = 100;
  private static final int VECTOR_GROUP = 150;
  private static final int SEXP_GROUP = 200;
  private static final int OTHER_OBJECT = 300;

  @Override
  public int compare(JvmMethod o1, JvmMethod o2) {
    if(o1.getPositionalFormals().size() != o2.getPositionalFormals().size()) {
      return o1.getPositionalFormals().size() - o2.getPositionalFormals().size();
    } else {
      for(int i=0;i!=o1.getPositionalFormals().size();++i) {
        Class c1 = o1.getPositionalFormals().get(i).getClazz();
        Class c2 = o2.getPositionalFormals().get(i).getClazz();
        int cmp = compareArguments(c1, c2);
        if(cmp != 0) {
          return cmp;
        }
      }
      return 0;
    }
  }

  private int compareArguments(Class c1, Class c2) {

    int g1 = group(c1);
    int g2 = group(c2);

    try {
      if(g1 != g2) {
        return g1 - g2;
      } else {
        return compareIntraGroup(g1, c1, c2);
      }
    } catch(Exception e) {
      throw new RuntimeException("Exception while comparing " + c1 + " and " + c2, e);
    }
  }

  /**
   * First, we have a general between ordering of Vectors/primitives (100), other SEXPs (200), \
   * and then other Java objects (300)
   */
  private int group(Class clazz) {

    if(clazz.isPrimitive() || clazz.equals(Complex.class) || clazz.equals(String.class) ||
        AtomicVector.class.isAssignableFrom(clazz)) {
      return ATOMIC_VECTOR_GROUP;
    } else if(Vector.class.isAssignableFrom(clazz)) {
      return VECTOR_GROUP;
    } else if(SEXP.class.isAssignableFrom(clazz)) {
      return SEXP_GROUP;
    } else {
      return OTHER_OBJECT;
    }
  }

  private int compareIntraGroup(int group, Class c1, Class c2) {
    if(group == ATOMIC_VECTOR_GROUP) {
      return compareVectors(c1, c2);
    } else {
      return compareClasses(c1, c2);
    }
  }

  private int compareClasses(Class c1, Class c2) {
    int a = c1.isAssignableFrom(c2) ? 1 : 0;
    int b =  c2.isAssignableFrom(c1) ? 1 : 0;
    return a - b;
  }

  private int compareVectors(Class c1, Class c2) {
    // first see if one Vector type subclasses the other,
    // that will determine the order if they have the same
    // vector types
    int cmp = compareClasses(c1, c2);
    if(cmp == 0) {
      Vector.Type t1 = vectorType(c1);
      Vector.Type t2 = vectorType(c2);

      cmp = t1.compareTo(t2);
    }
    return cmp;
  }

  private Vector.Type vectorType(Class clazz) {
    if(clazz.isPrimitive()) {
      return ScalarTypes.get(clazz).getVectorTypeInstance();
    } else if(clazz.equals(Complex.class)) {
      return ComplexVector.VECTOR_TYPE;
    } else if(clazz.equals(String.class)) {
      return StringVector.VECTOR_TYPE;
    } else {
      try {
        return (Vector.Type) clazz.getField("VECTOR_TYPE").get(null);
      } catch (Exception e) {
        throw new RuntimeException("Could not get VECTOR_TYPE from " + clazz.getName(), e);
      }
    }
  }

}
