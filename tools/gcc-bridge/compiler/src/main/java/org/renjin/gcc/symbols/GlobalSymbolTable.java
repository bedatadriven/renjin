package org.renjin.gcc.symbols;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.call.*;
import org.renjin.gcc.codegen.cpp.*;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.lib.SymbolFunction;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.lib.SymbolMethod;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.gcc.runtime.Mathlib;
import org.renjin.gcc.runtime.Stdlib;

import java.io.IOException;
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

  private ClassLoader linkClassLoader = getClass().getClassLoader();
  private TypeOracle typeOracle;
  private Map<String, CallGenerator> functions = Maps.newHashMap();
  private Map<String, GExpr> globalVariables = Maps.newHashMap();
  
  private Set<String> undefinedSymbols = Sets.newHashSet();

  public GlobalSymbolTable(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  public void setLinkClassLoader(ClassLoader linkClassLoader) {
    this.linkClassLoader = linkClassLoader;
  }

  @Override
  public CallGenerator findCallGenerator(GimpleFunctionRef ref) {
    String mangledName = ref.getName();
   
    CallGenerator generator = functions.get(mangledName);

    // Try to find the symbol on the classpath
    if(generator == null) {
      Optional<LinkSymbol> linkSymbol = null;
      try {
        linkSymbol = LinkSymbol.lookup(linkClassLoader, mangledName);
      } catch (IOException e) {
        throw new InternalCompilerException("Exception loading link symbol " + mangledName, e);
      }
      if(linkSymbol.isPresent()) {
        Method method = linkSymbol.get().loadMethod(linkClassLoader);
        generator = new FunctionCallGenerator(new StaticMethodStrategy(typeOracle, method));
        functions.put(mangledName, generator);
      }
    }
    
    // Otherwise return a generator that will throw an error at runtime
    if(generator == null) {
      generator = new UnsatisfiedLinkCallGenerator(mangledName);
      functions.put(mangledName, generator);
      
      System.err.println("Warning: undefined function " + mangledName + "; may throw exception at runtime");
    }
    
    return generator;
  }
  
  @Override
  public JExpr findHandle(GimpleFunctionRef ref) {
    CallGenerator callGenerator = findCallGenerator(ref);
    if(callGenerator instanceof MethodHandleGenerator) {
      return ((MethodHandleGenerator) callGenerator).getMethodHandle();
    } else {
      throw new UnsupportedOperationException("callGenerator: " + callGenerator);
    }
  }

  public void addDefaults() {

    addFunction("malloc", new MallocCallGenerator(typeOracle));
    addFunction("alloca", new MallocCallGenerator(typeOracle));
    addFunction("free", new FreeCallGenerator());
    addFunction("realloc", new ReallocCallGenerator(typeOracle));

    addFunction(CppStandardLibrary.NEW_OPERATOR, new MallocCallGenerator(typeOracle));
    addFunction(CppStandardLibrary.DELETE_OPERATOR, new FreeCallGenerator());
    
    addFunction("__builtin_malloc__", new MallocCallGenerator(typeOracle));
    addFunction("__builtin_free__", new MallocCallGenerator(typeOracle));
    addFunction("__builtin_memcpy", new MemCopyCallGenerator(typeOracle));
    addFunction("__builtin_memcpy__", new MemCopyCallGenerator(typeOracle));
    addFunction("__builtin_memset__", new MemSetGenerator(typeOracle));

    addFunction("__builtin_expect", new BuiltinExpectGenerator());
    
    addFunction("__cxa_allocate_exception", new MallocCallGenerator(typeOracle));
    addFunction(GuardAcquireGenerator.NAME, new GuardAcquireGenerator());
    addFunction(GuardReleaseGenerator.NAME, new GuardReleaseGenerator());
    addFunction(EhPointerCallGenerator.NAME, new EhPointerCallGenerator());
    addFunction(ThrowCallGenerator.NAME, new ThrowCallGenerator());
    addFunction(BeginCatchCallGenerator.NAME, new BeginCatchCallGenerator());
    addFunction(EndCatchGenerator.NAME, new EndCatchGenerator());
    
    addMethod("__builtin_log10__", Math.class, "log10");

    addFunction("memcpy", new MemCopyCallGenerator(typeOracle));
    addFunction("memcmp", new MemCmpCallGenerator(typeOracle));
    addFunction("memset", new MemSetGenerator(typeOracle));
    
    addMethods(Builtins.class);
    addMethods(Stdlib.class);
    addMethods(Mathlib.class);
  }

  public void addLibrary(SymbolLibrary lib) {
    for(SymbolFunction f : lib.getFunctions(typeOracle)) {
      addFunction(f.getAlias(), f.getCall());
    }
    for(SymbolMethod m : lib.getMethods()) {
      addMethod(m.getAlias(), m.getTargetClass(), m.getMethodName());
    }
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

  public void addFunction(String functionName, Method method) {
    Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method '%s' must be static", method);
    functions.put(functionName, new FunctionCallGenerator(new StaticMethodStrategy(typeOracle, method)));
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
  public GExpr getVariable(GimpleSymbolRef ref) {
    // Global variables are only resolved by name...
    if(ref.getName() == null) {
      return null;
    } else {
      GExpr expr = globalVariables.get(ref.getName());
      if(expr == null) {
        throw new InternalCompilerException("No such variable: " + ref);
      }
      return expr;
    }
  }
  
  public void addVariable(String name, GExpr expr) {
    globalVariables.put(name, expr);
  }
  
  public Set<Map.Entry<String, CallGenerator>> getFunctions() {
    return functions.entrySet();
  }
}
