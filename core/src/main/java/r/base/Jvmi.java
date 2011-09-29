package r.base;

import r.jvmi.annotations.Current;
import r.jvmi.annotations.Evaluate;
import r.jvmi.annotations.Primitive;
import r.jvmi.r2j.ClassBinding;
import r.jvmi.r2j.ClassFrame;
import r.lang.Context;
import r.lang.Environment;
import r.lang.EvalResult;
import r.lang.NamedValue;
import r.lang.Symbol;
import r.lang.exception.EvalException;

/**
 * Renjin-specific JVM interface primitives for interacting with 
 * Java/JVM objects
 * 
 */
public class Jvmi {

  private Jvmi() {
    
  }
  
  @Primitive("import")
  public static EvalResult importClass(@Current Context context, @Current Environment rho, 
        @Evaluate(false) Symbol className) {
        
    Class clazz;
    try {
      clazz = Class.forName(className.getPrintName());
    } catch (ClassNotFoundException e) {
      throw new EvalException("Cannot find class '%s'", className);
    }
    
    if(!context.getGlobals().securityManager.allowNewInstance(clazz)) {
      throw new EvalException("Permission to create a new instance of class '%s' has been denied by the security manager",
          className);
    }

    Environment env = Environment.createChildEnvironment(Environment.EMPTY, 
            new ClassFrame(ClassBinding.get(clazz)));
    
    rho.setVariable(Symbol.get(clazz.getSimpleName()), env);
    
    return EvalResult.invisible(env);
  }
}
