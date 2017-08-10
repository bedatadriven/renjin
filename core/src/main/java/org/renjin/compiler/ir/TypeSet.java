/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.compiler.ir;

import org.apache.commons.math.complex.Complex;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;


public class TypeSet {


  // Type Flags
  public static final int LIST = (1 << 1);
  public static final int NULL = (1 << 2);
  public static final int LOGICAL = (1 << 3);
  public static final int INT = (1 << 4);
  public static final int DOUBLE = (1 << 5);
  public static final int STRING = (1 << 6);
  public static final int COMPLEX = (1 << 7);
  public static final int RAW = (1 << 8);
  public static final int SYMBOL = (1 << 9);
  public static final int FUNCTION = (1 << 10);
  public static final int ENVIRONMENT = (1 << 11);
  public static final int PAIRLIST = (1 << 12);
  public static final int S4 = (1 << 13);
  public static final int ANY_ATOMIC_VECTOR = NULL | RAW | INT | LOGICAL | DOUBLE | COMPLEX | STRING;
  public static final int ANY_VECTOR = LIST | ANY_ATOMIC_VECTOR;
  public static final int ANY_TYPE = ANY_VECTOR | PAIRLIST | ENVIRONMENT | SYMBOL | FUNCTION;

  public static final int NUMERIC = INT | DOUBLE;

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
    } else if(constant instanceof Function) {
      return FUNCTION;
    } else if(constant instanceof S4Object) {
      return S4;
    } else {
      throw new UnsupportedOperationException("TODO: " + constant.getClass().getName());
    }
  }

  public static int of(Class type) {
    if (type.equals(int.class)) {
      return INT;

    } else if(type.equals(double.class)) {
      return DOUBLE;

    } else if (type.equals(boolean.class) || type.equals(Logical.class)) {
      return LOGICAL;

    } else if (type.equals(String.class)) {
      return STRING;

    } else if (type.equals(Complex.class)) {
      return COMPLEX;

    } else if (type.equals(IntVector.class)) {
      return INT;

    } else if (type.equals(ListVector.class)) {
      return LIST;

    } else if (type.equals(AtomicVector.class)) {
      return ANY_ATOMIC_VECTOR;
      
    } else if (type.equals(SEXP.class)) {
      return ANY_TYPE;

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

    } else if (type.equals(byte.class)) {
      return RAW;

    } else if (type.equals(String.class)) {
      return STRING;

    } else if (type.equals(Complex.class)) {
      return COMPLEX;

    } else if (type.equals(StringVector.class)) {
      return STRING;

    } else if(type.equals(IntVector.class)) {
      return INT;

    } else if(type.equals(DoubleVector.class)) {
      return DOUBLE;

    } else if(type.equals(ComplexVector.class)) {
      return COMPLEX;

    } else if(type.equals(RawVector.class)) {
      return RAW;

    } else if(type.equals(Vector.class)) {
      return ANY_VECTOR;

    } else if(type.equals(AtomicVector.class)) {
      return ANY_ATOMIC_VECTOR;

    } else if (type.equals(SEXP.class)) {
      return ANY_TYPE;

    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  /**
   * @return the unique S3 implicit class of this typeset, or {@code null} if it is not 
   * known to be a single class.
   */
  public static String implicitClass(int typeSet) {
    switch (typeSet) {
      case LIST:
        return "list";
      case NULL:
        return "NULL";
      case INT:
        return "integer";
      case DOUBLE:
        return "double";
      case LOGICAL:
        return "logical";
      case STRING:
        return "character";
      case COMPLEX:
        return "complex";
      case RAW:
        return "raw";
      case SYMBOL:
        return "name";
      case FUNCTION:
        return "function";
      case ENVIRONMENT:
        return "environment";
      case PAIRLIST:
        return "pairlist";
      default:
        return null;
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
    appendType(s, "S4", mask, S4);
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


  public static boolean isDefinitelyNumeric(ValueBounds subscript) {
    return isDefinitelyNumeric(subscript.getTypeSet());
  }

  public static boolean isDefinitelyNumeric(int typeSet) {
    return (typeSet & NUMERIC) != 0 &&
        (typeSet & ~NUMERIC) == 0;
  }

  public static int widestVectorType(int x, int y) {
    return Math.max(x, y);
  }

  public static int elementOf(int typeSet) {

    // If typeset is not limited to atomic, then could be anything
    if((typeSet & ~ANY_ATOMIC_VECTOR) != 0) {
      return ANY_TYPE;
    }

    // if we are limited to atomic, then the elements will be the same type
    return typeSet;

  }

  public static boolean isDefinitelyAtomic(int typeSet) {
    return (typeSet & ANY_ATOMIC_VECTOR) != 0 &&
        (typeSet & ~ANY_ATOMIC_VECTOR) == 0;
  }
}
