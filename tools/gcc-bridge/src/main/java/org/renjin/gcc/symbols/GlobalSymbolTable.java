package org.renjin.gcc.symbols;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.call.FunctionCallGenerator;
import org.renjin.gcc.codegen.call.StaticMethodCallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.runtime.Builtins;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * Provides mapping of function and variable symbols that are globally visible. 
 * 
 * <p>This includes built-in symbols, externally provided methods or variables, and 
 * functions and global variables with external linkage.</p>
 */
public class GlobalSymbolTable {

  private GeneratorFactory generators;
  private Map<String, CallGenerator> functions = Maps.newHashMap();
  private Map<String, ExprGenerator> globalVariables = Maps.newHashMap();

  public GlobalSymbolTable(GeneratorFactory generators) {
    this.generators = generators;
  }

  public CallGenerator getCallGenerator(GimpleFunctionRef ref, CallingConvention callingConvention) {
    String mangledName = callingConvention.mangleFunctionName(ref.getName());
    CallGenerator generator = functions.get(mangledName);
    if(generator == null) {
      throw new UnsupportedOperationException("Could not find function '" + mangledName + "'");
    }
    return generator;
  }
  
  public Handle findHandle(GimpleFunctionRef ref, CallingConvention callingConvention) {
    FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) getCallGenerator(ref, callingConvention);
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

  public void addMethod(String functionName, Class<?> declaringClass) {
    addFunction(functionName, findMethod(declaringClass, functionName));
  }
  
  public void addMethod(String functionName, Class<?> declaringClass, String methodName) {
    addFunction(functionName, findMethod(declaringClass, methodName));
  }

  public void addFunction(String className, FunctionGenerator function) {
    functions.put(function.getMangledName(), new FunctionCallGenerator(function));
  }
  
  public void addFunction(String functionName, Method method) {
    Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method '%s' must be static", method);
    functions.put(functionName, new StaticMethodCallGenerator(generators, method));
  }

  public void addMethods(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        addFunction(method.getName(), method);
      }
    }
  }

  private Method findMethod(Class<?> declaringClass, String methodName) {
    for (Method method : declaringClass.getMethods()) {
      if(method.getName().equals(methodName)) {
        return method;
      }
    }
    throw new IllegalArgumentException(format("No method named '%s' in %s", methodName, declaringClass.getName()));
  }

  public ExprGenerator getVariable(String name) {
    return globalVariables.get(name);
  }
  
  public void addVariable(String name, ExprGenerator exprGenerator) {
    globalVariables.put(name, exprGenerator);
  }
  
  public Set<Map.Entry<String, CallGenerator>> getFunctions() {
    return functions.entrySet();
  }
}
