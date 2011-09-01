package r.jvmi.wrapper;

import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.EvalResult;
import r.lang.ExternalExp;
import r.lang.FunctionCall;
import r.lang.IntVector;
import r.lang.Logical;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Vector;

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
    Vector vector = checkedSubClassAndAssertScalar(exp);
    return vector.getElementAsInt(0);
  }
  
  /**
   * 
   * @param args
   * @return the next argument in the argument pair list.
   * @throws ArgumentException if there is no next argument
   */
  public static PairList.Node nextArgument(PairList args) {
    if(args == Null.INSTANCE) {
      throw new ArgumentException();
    }
    return ((PairList.Node)args).getNextNode();
  }
  
  /**
   * 
   * @param args
   * @return the value of the argument 
   * @throws ArgumentException if there is no current argument
   */
  public static SEXP argumentValue(PairList args) {
    if(!(args instanceof PairList.Node)) {
      throw new ArgumentException();
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
      if(provided instanceof Promise) {
        provided = ((Promise) provided).force().getExpression();
      }
      return (Vector) FunctionCall
        .newCall(Symbol.AS_CHARACTER, provided)
          .evalToExp(context, rho);
    }
  }

  public static boolean convertToBooleanPrimitive(SEXP exp) {
    Vector vector = checkedSubClassAndAssertScalar(exp);
    if(vector.isElementNA(0)) {
      throw new UnsupportedOperationException("an NA value cannot be cast to a Java boolean value");
    }
    return vector.getElementAsLogical(0) == Logical.TRUE;  
  }
  
  public static double convertToDoublePrimitive(SEXP exp) {
    Vector vector = checkedSubClassAndAssertScalar(exp);
    if(vector.isElementNA(0)) {
      throw new UnsupportedOperationException("an NA value cannot be cast to a Java boolean value");
    }
    return vector.getElementAsDouble(0);
  }
  
  
  private static Vector checkedSubClassAndAssertScalar(SEXP exp) {
    if(exp.length() != 1) {
      throw new ArgumentException();
    }
    if(!(exp instanceof Vector)) {
      throw new ArgumentException();
    }
    return (Vector)exp;
  }
  
  public static <T> T unwrapExternal(SEXP exp) {
    try {
      ExternalExp<T> external = (ExternalExp<T>)exp;
      return (T)external.getValue();
    } catch(ClassCastException e) {
      throw new ArgumentException();
    }
  }
  
  
  public static EvalResult wrapResult(SEXP exp) {
    return EvalResult.visible(exp);
  }
  
  public static EvalResult wrapResult(int i) {
    return EvalResult.visible(new IntVector(i));
  }
  
  public static EvalResult wrapResult(Integer i) {
    return EvalResult.visible(new IntVector(i == null ? IntVector.NA : i));
  }
  
  public static EvalResult wrapResult(String s) {
    return EvalResult.visible(new StringVector(s));
  }    
 
  public static EvalResult wrapResult(boolean b) {
    return EvalResult.visible(new LogicalVector(b));
  }
 
  public static EvalResult wrapResult(long result) {
    return EvalResult.visible(new DoubleVector((double)result));
  }
  
  public static EvalResult wrapResult(int [] result) {
    return EvalResult.visible(new IntVector(result));
  }
  
  public static EvalResult wrapResult(Logical result) {
    return EvalResult.visible(new LogicalVector(result));
  }
}
