package org.renjin.primitives.annotations.processor;

import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.eval.DispatchChain;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;
import org.renjin.sexp.PairList.Node;


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
      ExternalExp<T> external = (ExternalExp<T>)exp;
      return (T)external.getValue();
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


}
