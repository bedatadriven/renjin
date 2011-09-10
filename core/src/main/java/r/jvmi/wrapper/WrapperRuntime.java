package r.jvmi.wrapper;

import r.base.Calls;
import r.base.ClosureDispatcher;
import r.base.dispatch.DispatchChain;
import r.lang.Closure;
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
import r.lang.exception.EvalException;

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
    throw new ArgumentException();
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
  
  public static Vector convertToVector(SEXP exp) {
    try {
      return (Vector)exp;
    } catch(ClassCastException e) {
      throw new ArgumentException();
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
  
  public static EvalResult wrapResult(double d) {
    return EvalResult.visible(new DoubleVector(d));
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
  
  /**
   * There are a few primitive functions (`[[` among them) which are proper builtins, but attempt
   * to dispatch on the class of their first argument before going ahead with the default implementation.
   * 
   * @param context
   * @param rho
   * @param name the name of the function
   * @param args the original args from the FunctionCall
   * @param object evaluated first argument
   * @return
   */
  public static EvalResult tryDispatchFromPrimitive(Context context, Environment rho, FunctionCall call, 
      String name, SEXP object, PairList args) {
    
    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }
    
    Vector classVector = (Vector)object.getAttribute(Symbol.CLASS);
    if(classVector.length() == 0) {
      return null;
    }

    DispatchChain chain = DispatchChain.newChain(rho, name, classVector);
    if(chain == null) {
      return null;
    }

    PairList newArgs = rebuildArgs(object, args);

    FunctionCall newCall = FunctionCall.newCall(chain.getMethodSymbol(), newArgs);

    ClosureDispatcher dispatcher = new ClosureDispatcher(context, rho, newCall);
    return dispatcher.apply(chain, newArgs);
  }

  private static PairList rebuildArgs(SEXP object, PairList args) {
    PairList.Node restOfArguments = (PairList.Node) args;
    PairList newArgs = new PairList.Node(new Promise(restOfArguments.getValue(), object), 
        restOfArguments.hasNextNode() ? restOfArguments.getNextNode() : Null.INSTANCE);
    return newArgs;
  }

  public static EvalResult tryDispatchGroupFromPrimitive(Context context, Environment rho, FunctionCall call, 
      String group, String name, SEXP object, PairList args) {

    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }
    
    PairList.Builder newArgs = new PairList.Builder();
    newArgs.add(args.getRawTag(), object);
    if(((PairList.Node)args).hasNextNode()) {
      for(PairList.Node node : ((PairList.Node)args).getNextNode().nodes()) {
        newArgs.add(node.getRawTag(), node.getValue().evalToExp(context, rho));
      }
    }
    EvalResult dispatched = Calls.DispatchGroup(group, call, name, newArgs.build(), context, rho);
    
    return dispatched;

  }
  
  public static PairList evaluateList(Context context, Environment rho, PairList args) {
    PairList.Builder evaled = new PairList.Builder();
    for(PairList.Node node : args.nodes()) {
      evaled.add(node.getRawTag(), node.getValue().evalToExp(context, rho));
    }
    return evaled.build();
  }


}
