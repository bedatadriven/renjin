package org.renjin.primitives.annotations.processor;

import java.util.List;

import org.renjin.primitives.Primitives.Entry;


import r.jvmi.binding.JvmMethod;

/**
 * Strategy for generating a wrapper in the case where the declared JVM method
 * expects to handle the argument processing itself. 
 * 
 * <p>For example:
 * 
 * <br>
 * <code>
 * public static SEXP UseMethod(Context context, Environment rho, FunctionCall call);
 * </code>
 * 
 * <br>
 * The generated code simply passes the call on.
 * 
 * 
 * @author alex
 *
 */
public class PassThrough extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    return overloads.size() == 1 && overloads.get(0).acceptsCall();
  }
  


  @Override
  protected void generateMethods(Entry entry, WrapperSourceWriter s, List<JvmMethod> overloads) {
    JvmMethod method = overloads.get(0);
    

    s.println("@Override");
    s.writeBeginBlock("public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {");
    
    StringBuilder call = new StringBuilder();
    call.append(method.getDeclaringClass().getName()).append(".")
      .append(method.getName())
      .append("(context, rho, call)");
    
    if(method.returnsVoid()) {
      s.writeStatement(call.toString());
      s.writeStatement("context.setInvisibleFlag()");
      s.writeStatement("return r.lang.Null.INSTANCE;");
    } else {
      s.writeStatement("return " + call.toString());
    }
    
    s.writeCloseBlock();

  }
  
}
