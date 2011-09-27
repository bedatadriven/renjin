package r.jvmi.wrapper.generator;

import java.util.List;

import r.base.Primitives.Entry;
import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.WrapperSourceWriter;

/**
 * Strategy for generating a wrapper in the case where the declared JVM method
 * expects to handle the argument processing itself. 
 * 
 * <p>For example:
 * 
 * <br>
 * <code>
 * public static EvalResult UseMethod(Context context, Environment rho, FunctionCall call);
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
  protected void generateCall(Entry entry, WrapperSourceWriter s, List<JvmMethod> overloads) {
    JvmMethod method = overloads.get(0);
    
    s.writeStatement(callStatement(method, new ArgumentList("context", "rho", "call")));
    
    if(method.returnsVoid()) {
      s.writeStatement("return EvalResult.NON_PRINTING_NULL;");
    }
    
  }
  
}
