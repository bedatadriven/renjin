package org.renjin.gcc.symbols;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.*;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.gcc.runtime.Mathlib;
import org.renjin.gcc.runtime.Stdlib;

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
public class GlobalSymbolTable implements SymbolTable {

  private TypeOracle typeOracle;
  private Map<String, CallGenerator> functions = Maps.newHashMap();
  private Map<String, ExprGenerator> globalVariables = Maps.newHashMap();

  public GlobalSymbolTable(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  @Override
  public CallGenerator findCallGenerator(GimpleFunctionRef ref, CallingConvention callingConvention) {
    String mangledName = callingConvention.mangleFunctionName(ref.getName());
    CallGenerator generator = functions.get(mangledName);
    if(generator == null) {
      throw new UnsupportedOperationException("Could not find function '" + mangledName + "'");
    }
    return generator;
  }
  
  @Override
  public Handle findHandle(GimpleFunctionRef ref, CallingConvention callingConvention) {
    FunctionCallGenerator functionCallGenerator = (FunctionCallGenerator) findCallGenerator(ref, callingConvention);
    return functionCallGenerator.getHandle();
  }


  public void addDefaults() {

    addFunction("malloc", new MallocCallGenerator(typeOracle));
    addFunction("free", new FreeCallGenerator());
    addFunction("realloc", new ReallocCallGenerator(typeOracle));

    addFunction("__builtin_malloc__", new MallocCallGenerator(typeOracle));
    addFunction("__builtin_free__", new MallocCallGenerator(typeOracle));
    addFunction("__builtin_memcpy", new MemCopyCallGenerator());
    
    addFunction("memcpy", new MemCopyCallGenerator());
    addFunction("memcmp", new MemCmpCallGenerator());
    addFunction("memset", new MemSetGenerator());
    
    addMethods(Builtins.class);
    addMethods(Stdlib.class);
    addMethods(Mathlib.class);
  }
  

  public void addMethod(String functionName, Class<?> declaringClass) {
    addFunction(functionName, findMethod(declaringClass, functionName));
  }
  
  public void addMethod(String functionName, Class<?> declaringClass, String methodName) {
    addFunction(functionName, findMethod(declaringClass, methodName));
  }

  public void addFunction(String name, CallGenerator callGenerator) {
    functions.put(name, callGenerator);
  }
  
  public void addFunction(String className, FunctionGenerator function) {
    functions.put(function.getMangledName(), new FunctionCallGenerator(function));
  }
  
  public void addFunction(String functionName, Method method) {
    Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method '%s' must be static", method);
    functions.put(functionName, new StaticMethodCallGenerator(typeOracle, method));
  }

  public void addMethods(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        
        // skip methods that have been @Deprecated
        if(method.getAnnotation(Deprecated.class) != null) {
          continue;
        }
        
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

  @Override
  public ExprGenerator getVariable(GimpleSymbolRef ref) {
    // Global variables are only resolved by name...
    if(ref.getName() == null) {
      return null;
    } else {
      ExprGenerator exprGenerator = globalVariables.get(ref.getName());
      if(exprGenerator == null) {
        throw new InternalCompilerException("No such variable: " + ref);
      }
      return exprGenerator;
    }
  }
  
  public void addVariable(String name, ExprGenerator exprGenerator) {
    globalVariables.put(name, exprGenerator);
  }
  
  public Set<Map.Entry<String, CallGenerator>> getFunctions() {
    return functions.entrySet();
  }
}
