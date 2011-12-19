package r.jvmi.wrapper;

import r.base.Calls;
import r.base.ClosureDispatcher;
import r.base.dispatch.DispatchChain;
import r.lang.*;
import r.lang.PairList.Node;

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
        provided = ((Promise) provided).force();
      }
      return (Vector) context.evaluate( FunctionCall
        .newCall(Symbols.AS_CHARACTER, provided), rho);
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
  
  public static SEXP wrapResult(int i) {
    return new IntVector(i);
  }
  
  public static SEXP wrapResult(Integer i) {
    return new IntVector(i == null ? IntVector.NA : i);
  }
  
  public static SEXP wrapResult(String s) {
    return new StringVector(s);
  }    
 
  public static SEXP wrapResult(boolean b) {
    return new LogicalVector(b);
  }
  
  public static SEXP wrapResult(float f) {
    return new DoubleVector(f);
  }
  
  public static SEXP wrapResult(double d) {
    return new DoubleVector(d);
  }
 
  public static SEXP wrapResult(long result) {
    return new DoubleVector((double)result);
  }
  
  public static SEXP wrapResult(int [] result) {
    return new IntVector(result);
  }
  
  public static SEXP wrapResult(Logical result) {
    return new LogicalVector(result);
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
  public static SEXP tryDispatchFromPrimitive(Context context, Environment rho, FunctionCall call, 
      String name, SEXP object, PairList args) {
    
    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }
    
    Vector classVector = (Vector)object.getAttribute(Symbols.CLASS);
    if(classVector.length() == 0) {
      return null;
    }

    DispatchChain chain = DispatchChain.newChain(rho, name, classVector);
    if(chain == null) {
      return null;
    }

    PairList newArgs = reassembleAndEvaluateArgs(object, args, context, rho);

    FunctionCall newCall = new FunctionCall(chain.getMethodSymbol(), newArgs);

    ClosureDispatcher dispatcher = new ClosureDispatcher(context, rho, newCall);
    return dispatcher.apply(chain, newArgs);
  }

  private static PairList reassembleAndEvaluateArgs(SEXP object, PairList args, Context context, Environment rho) {
    PairList.Builder newArgs = new PairList.Builder();
    Node firstArg = (PairList.Node)args;
    newArgs.add(firstArg.getRawTag(), object);
    
    args = firstArg.getNext();
    
    for(PairList.Node node : args.nodes()) {
      newArgs.add(node.getRawTag(), context.evaluate( node.getValue(), rho));
    }
    
    return newArgs.build();
  }

  public static SEXP tryDispatchGroupFromPrimitive(Context context, Environment rho, FunctionCall call,
      String group, String name, SEXP s0) {

    PairList newArgs = new PairList.Node(s0, Null.INSTANCE);

    return Calls.DispatchGroup(group, call, name, newArgs, context, rho);
  }


  public static SEXP tryDispatchGroupFromPrimitive(Context context, Environment rho, FunctionCall call,
      String group, String name, SEXP s0, SEXP s1) {

    PairList newArgs = new PairList.Node(s0, new PairList.Node(s1, Null.INSTANCE));

    return Calls.DispatchGroup(group, call, name, newArgs, context, rho);
  }


}
