package org.renjin.gcc.codegen.call;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Provides a mapping from function names to {@code CallGenerator}s
 */
public class FunctionTable {
  
  private Map<String, MethodCallGenerator> functions = new HashMap<String, MethodCallGenerator>();


  public CallGenerator find(GimpleFunctionRef ref) {
    MethodCallGenerator generator = functions.get(ref.getName());
    if(generator == null) {
      throw new UnsupportedOperationException("Could not find function '" + ref.getName() + "'");
    }
    return generator;
  }


  public void addDefaults() {

    // G77 builtins
    addMethod("__builtin_sin__", Math.class, "sin");
    addMethod("__builtin_log__", Math.class, "log");
    addMethod("__builtin_cos__", Math.class, "cos");
    addMethod("__builtin_sqrt__", Math.class, "sqrt");
    addMethod("__builtin_pow__", Math.class, "pow");
    addMethod("pow", Math.class, "pow");

    //addMethod("__builtin_copysign__", Math.class, "copySign");

    addMethod("sqrt", Math.class);
    addMethod("floor", Math.class);

    //addReferenceClass(Builtins.class);
  }

  private void addMethod(String functionName, Class<Math> declaringClass) {
    add(functionName, findMethod(declaringClass, functionName));
  }
  
  private void addMethod(String functionName, Class<Math> declaringClass, String methodName) {
    add(functionName, findMethod(declaringClass, methodName));
  }

  public void add(String className, String functionName, FunctionGenerator function) {
    MethodCallGenerator generator = new MethodCallGenerator(className, 
        function.getMangledName(),
        function.parameterTypes(),
        function.returnType());

    functions.put(functionName, generator);
  }
  
  public void add(String functionName, Method method) {
    Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method '%s' must be static", method);

    MethodCallGenerator generator = new MethodCallGenerator(
        Type.getInternalName(method.getDeclaringClass()),
        method.getName(),
        Type.getArgumentTypes(method),
        Type.getReturnType(method));
    
    functions.put(functionName, generator);
  }


  private Method findMethod(Class<Math> declaringClass, String methodName) {
    for (Method method : declaringClass.getMethods()) {
      if(method.getName().equals(methodName)) {
        return method;
      }
    }
    throw new IllegalArgumentException(format("No method named '%s' in %s", methodName, declaringClass.getName()));
  }

}
