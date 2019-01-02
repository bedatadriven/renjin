/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.invoke.codegen;

import org.renjin.eval.Context;
import org.renjin.primitives.Deparse;
import org.renjin.sexp.*;


/**
 * 
 * Utility functions used by generated function wrappers at runtime.
 *  
 * @author alex
 *
 */
public class WrapperRuntime {
  
  public static String convertToString(SEXP exp) {
    if(exp == Null.INSTANCE) {
      return null;
    }
    Vector vector = checkedSubClassAndAssertScalar(exp);
    return vector.getElementAsString(0);
  }
  
  public static int convertToInt(SEXP exp) {
    if(exp instanceof DoubleVector || exp instanceof IntVector || exp instanceof LogicalVector) {
      return ((Vector)exp).getElementAsInt(0);
    }
    throw new ArgumentException("expected int");
  }

  /**
   *
   * @param args
   * @return the value of the argument
   * @throws ArgumentException if there is no current argument
   */
  public static SEXP argumentValue(PairList args) {
    if(!(args instanceof PairList.Node)) {
      throw new ArgumentException("too few arguments");
    }
    return ((PairList.Node)args).getValue();
  }

  private Integer convertToInteger(SEXP exp) {

    if(exp == Null.INSTANCE) {
      return null;
    }
    Vector vector = checkedSubClassAndAssertScalar(exp);
    if(vector.isElementNA(0)) {
      return null;
    } else {
      return vector.getElementAsInt(0);
    }
  }

  public static Vector invokeAsCharacter(Context context, Environment rho,
      SEXP provided) {
    if(provided == Null.INSTANCE) {
      return Null.INSTANCE;
    } else {
      provided = provided.force(context);
      return (Vector) context.evaluate( FunctionCall
        .newCall(Symbols.AS_CHARACTER, provided), rho);
    }
  }

  public static boolean convertToBooleanPrimitive(SEXP exp) {
    if(exp.length() == 0) {
      return false;
    }
    return exp.asLogical() == Logical.TRUE;
  }
  
  public static Vector convertToVector(SEXP exp) {
    if(exp instanceof Vector) {
      return (Vector)exp;
    } else if(exp instanceof Symbol) {
      return new StringArrayVector( ((Symbol) exp).getPrintName() );
    } else if(exp instanceof FunctionCall) {
      return new StringArrayVector( Deparse.deparseExp(null, exp) );
    } else {
      throw new ArgumentException("expected vector");
    }
  }
  
  public static double convertToDoublePrimitive(SEXP exp) {
    Vector vector = checkedSubClassAndAssertScalar(exp);
    return vector.getElementAsDouble(0);
  }

  public static double convertToRawPrimitive(SEXP exp) {
    Vector vector = checkedSubClassAndAssertScalar(exp);
    return vector.getElementAsByte(0);
  }
  
  public static float convertToFloatPrimitive(SEXP exp) {
    Vector vector = checkedSubClassAndAssertScalar(exp);
    return (float)vector.getElementAsDouble(0);
  }
  
  private static Vector checkedSubClassAndAssertScalar(SEXP exp) {
    if(exp.length() != 1) {
      throw new ArgumentException("expected vector of length 1");
    }
    if(!(exp instanceof Vector)) {
      throw new ArgumentException("expected vector of length 1");
    }
    return (Vector)exp;
  }

  public static <T> T unwrapExternal(SEXP exp) {
    try {
      ExternalPtr<T> external = (ExternalPtr<T>)exp;
      return (T)external.getInstance();
    } catch(ClassCastException e) {
      throw new ArgumentException("expected external object");
    }
  }
  
  public static SEXP wrapResult(int i) {
    return new IntArrayVector(i);
  }
  
  public static SEXP wrapResult(Integer i) {
    return new IntArrayVector(i == null ? IntVector.NA : i);
  }
  
  public static SEXP wrapResult(String s) {
    return StringVector.valueOf(s);
  }    
 
  public static SEXP wrapResult(boolean b) {
    return new LogicalArrayVector(b);
  }
  
  public static SEXP wrapResult(float f) {
    return new DoubleArrayVector(f);
  }
  
  public static SEXP wrapResult(double d) {
    return new DoubleArrayVector(d);
  }
 
  public static SEXP wrapResult(long result) {
    return new DoubleArrayVector((double)result);
  }
  
  public static SEXP wrapResult(int [] result) {
    return new IntArrayVector(result);
  }
  
  public static SEXP wrapResult(Logical result) {
    return new LogicalArrayVector(result);
  }

  public static SEXP maybeConvertToStringVector(Context context, SEXP vector) {
    if(vector instanceof Symbol) {
      return StringVector.valueOf(((Symbol)vector).getPrintName());
    } else if(vector instanceof FunctionCall) {
      return StringVector.valueOf(Deparse.deparseExp(context, vector));
    }
    return vector;
  }

  public static boolean isEmptyOrNull(SEXP vector) {
    if(vector instanceof Vector) {
      return ((Vector) vector).length() == 0;
    }
    return false;
  }

  public static boolean isEnvironmentOrEnvironmentSubclass(SEXP object) {
    if(object instanceof Environment) {
      return true;
    }
    if(object instanceof S4Object) {
      return object.getAttribute(Symbols.DOT_XDATA) instanceof Environment;
    }
    return false;
  }

  public static Environment unwrapEnvironmentSuperClass(SEXP object) {
    if(object instanceof Environment) {
      return (Environment) object;
    }
    if(object instanceof S4Object) {
      return (Environment) object.getAttribute(Symbols.DOT_XDATA);
    }
    throw new IllegalArgumentException();
  }
}
