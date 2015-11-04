package org.renjin.gcc.codegen.call;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.runtime.Builtins;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static java.lang.String.format;

/**
 * Provides a mapping from function names to {@code CallGenerator}s
 */
public class FunctionTable {
  
  private GeneratorFactory generators;
  private Map<String, CallGenerator> functions = Maps.newHashMap();

  public FunctionTable(GeneratorFactory generators) {
    this.generators = generators;
  }

  public CallGenerator find(GimpleFunctionRef ref, CallingConvention callingConvention) {
    String mangledName = callingConvention.mangleFunctionName(ref.getName());
    CallGenerator generator = functions.get(mangledName);
    if(generator == null) {
      throw new UnsupportedOperationException("Could not find function '" + mangledName + "'");
    }
    return generator;
  }
  
  public Handle findHandle(GimpleFunctionRef ref, CallingConvention callingConvention) {
    FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) find(ref, callingConvention);
    return functionCallGenerator.getHandle();
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

    addMethods(Builtins.class);
  }

  public void addMethod(String functionName, Class<Math> declaringClass) {
    add(functionName, findMethod(declaringClass, functionName));
  }
  
  public void addMethod(String functionName, Class<Math> declaringClass, String methodName) {
    add(functionName, findMethod(declaringClass, methodName));
  }

  public void add(String className, FunctionGenerator function) {
    FunctionCallGenerator generator = new FunctionCallGenerator(className, 
        function.getMangledName(),
        function.getParamGenerators(),
        function.getReturnGenerator());

    functions.put(function.getMangledName(), generator);
  }
  
  public void add(String functionName, Method method) {
    Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method '%s' must be static", method);
//
//    MethodCallGenerator generator = new MethodCallGenerator(
//        Type.getInternalName(method.getDeclaringClass()),
//        method.getName(),
//        createParamGenerators(method),
//        createReturnGenerator(method));
//    
    functions.put(functionName, new StaticMethodCallGenerator(generators, method));
  }

  public void addMethods(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        add(method.getName(), method);
      }
    }
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
