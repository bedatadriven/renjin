package org.renjin.compiler.ir;

import org.apache.commons.math.complex.Complex;
import org.renjin.sexp.*;


public class TypeSet {


  // Type Flags
  public static final int LIST = (1 << 1);
  public static final int NULL = (1 << 2);
  public static final int INT = (1 << 3);
  public static final int DOUBLE = (1 << 4);
  public static final int LOGICAL = (1 << 5);
  public static final int STRING = (1 << 6);
  public static final int COMPLEX = (1 << 7);
  public static final int RAW = (1 << 8);
  public static final int SYMBOL = (1 << 9);
  public static final int FUNCTION = (1 << 10);
  public static final int ENVIRONMENT = (1 << 11);
  public static final int PAIRLIST = (1 << 12);
  public static final int ANY_ATOMIC_VECTOR = NULL | RAW | INT | LOGICAL | DOUBLE | COMPLEX | STRING;
  public static final int ANY_VECTOR = LIST | ANY_ATOMIC_VECTOR;
  public static final int ANY_TYPE = ANY_VECTOR | PAIRLIST | ENVIRONMENT | SYMBOL | FUNCTION;


  public static int of(SEXP constant) {
    if(constant instanceof ListVector) {
      return LIST;
    } else if(constant instanceof Null) {
      return NULL;
    } else if(constant instanceof LogicalVector) {
      return LOGICAL;
    } else if(constant instanceof RawVector) {
      return  RAW;
    } else if(constant instanceof IntVector) {
      return INT;
    } else if(constant instanceof DoubleVector) {
      return DOUBLE;
    } else if(constant instanceof ComplexVector) {
      return COMPLEX;
    } else if(constant instanceof StringVector) {
      return STRING;
    } else if(constant instanceof Symbol) {
      return SYMBOL;
    } else if(constant instanceof Environment) {
      return ENVIRONMENT;
    } else if(constant instanceof PairList) {
      return PAIRLIST;
    } else if(constant instanceof Closure) {
      return FUNCTION;
    } else {
      throw new UnsupportedOperationException("TODO: " + constant.getClass().getName());
    }
  }

  public static int of(Class type) {
    if (type.equals(int.class)) {
      return INT;

    } else if(type.equals(double.class)) {
      return DOUBLE;

    } else if (type.equals(boolean.class)) {
      return LOGICAL;

    } else if (type.equals(String.class)) {
      return STRING;

    } else if (type.equals(Complex.class)) {
      return COMPLEX;

    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  public static int accepts(Class type) {
    if (type.equals(int.class)) {
      return INT | LOGICAL;

    } else if(type.equals(double.class)) {
      return INT | LOGICAL | DOUBLE;

    } else if (type.equals(boolean.class)) {
      return LOGICAL;

    } else if (type.equals(String.class)) {
      return STRING;

    } else if (type.equals(Complex.class)) {
      return COMPLEX;

    } else if (type.equals(SEXP.class)) {
      return ANY_TYPE;

    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  public static boolean matches(Class clazz, int typeSet) {
    // compute the set of bits that we will accept
    int mask = accepts(clazz);
    
    // compute the set of bits we will NOT accept
    int inverseMask = mask ^ ANY_TYPE;
    
    // Make sure that no types are possible that are NOT acceptable for this argument
    return (typeSet & inverseMask) == 0;
  }

  public static String toString(int mask) {

    if(mask == ANY_TYPE) {
      return "*";
    }

    StringBuilder s = new StringBuilder();
    appendType(s, "list", mask, LIST);
    appendType(s, "null", mask, NULL);
    appendType(s, "int", mask, INT);
    appendType(s, "double", mask, DOUBLE);
    appendType(s, "logical", mask, LOGICAL);
    appendType(s, "character", mask, STRING);
    appendType(s, "complex", mask, COMPLEX);
    appendType(s, "raw", mask, RAW);
    appendType(s, "symbol", mask, SYMBOL);
    appendType(s, "function", mask, FUNCTION);
    appendType(s, "environment", mask, ENVIRONMENT);
    appendType(s, "pairlist", mask, PAIRLIST);
    return s.toString();
  }
  
  private static void appendType(StringBuilder sb, String name, int mask, int bit) {
    if( (mask & bit) != 0) {
      if(sb.length() > 1) {
        sb.append("|");
      }
      sb.append(name);
    }
  }

}
