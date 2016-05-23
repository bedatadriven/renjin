package org.renjin.gcc.symbols;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.objectweb.asm.Handle;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.call.*;
import org.renjin.gcc.codegen.cpp.BeginCatchCallGenerator;
import org.renjin.gcc.codegen.cpp.EhPointerCallGenerator;
import org.renjin.gcc.codegen.cpp.EndCatchGenerator;
import org.renjin.gcc.codegen.cpp.ThrowCallGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.lib.SymbolFunction;
import org.renjin.gcc.codegen.lib.SymbolLibrary;
import org.renjin.gcc.codegen.lib.SymbolMethod;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.expr.GimpleSymbolRef;
import org.renjin.gcc.link.LinkSymbol;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.gcc.runtime.Mathlib;
import org.renjin.gcc.runtime.Stdlib;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
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
  private Map<String, Expr> globalVariables = Maps.newHashMap();

  public GlobalSymbolTable(TypeOracle typeOracle) {
    this.typeOracle = typeOracle;
  }

  public void setLinkClassLoader(ClassLoader linkClassLoader) {
    this.linkClassLoader = linkClassLoader;
  }

  @Override
  public CallGenerator findCallGenerator(GimpleFunctionRef ref, List<GimpleExpr> operands) {
    String mangledName = ref.getName();
    Optional<CallGenerator> generator = findCallGenerator(mangledName);
    
    if(generator.isPresent()) {
      return generator.get();
    }
    
    return new FunctionCallGenerator(new UnimplCallGenerator(mangledName));
  }

  private Optional<CallGenerator> findCallGenerator(String mangledName) {
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
    return Optional.fromNullable(generator);
  }


  public boolean isFunctionDefined(String name) {
    return findCallGenerator(name).isPresent();
  }
  
  @Override
  public Handle findHandle(GimpleFunctionRef ref) {
    CallGenerator callGenerator = findCallGenerator(ref, null);
    if(callGenerator instanceof FunctionCallGenerator) {
      return ((FunctionCallGenerator) callGenerator).getStrategy().getMethodHandle();
    } else {
      throw new UnsupportedOperationException("callGenerator: " + callGenerator);
    }
  }

  public void addDefaults() {

    addFunction("malloc", new MallocCallGenerator(typeOracle));
    addFunction("free", new FreeCallGenerator());
    addFunction("realloc", new ReallocCallGenerator(typeOracle));

    addFunction("__builtin_malloc__", new MallocCallGenerator(typeOracle));
    addFunction("__builtin_free__", new MallocCallGenerator(typeOracle));
    addFunction("__builtin_memcpy", new MemCopyCallGenerator(typeOracle));
    addFunction("__builtin_memcpy__", new MemCopyCallGenerator(typeOracle));
    addFunction("__builtin_memset__", new MemSetGenerator(typeOracle));

    addFunction("__cxa_allocate_exception", new MallocCallGenerator(typeOracle));
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
  
  public void addFunction(FunctionGenerator function) {
    functions.put(function.getMangledName(), new FunctionCallGenerator(function));
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
  public Expr getVariable(GimpleSymbolRef ref) {
    // Global variables are only resolved by name...
    if(ref.getName() == null) {
      return null;
    } else {
      Expr expr = globalVariables.get(ref.getName());
      if(expr == null) {
        throw new InternalCompilerException("No such variable: " + ref);
      }
      return expr;
    }
  }
  
  public void addVariable(String name, Expr expr) {
    globalVariables.put(name, expr);
  }
  
  public Set<Map.Entry<String, CallGenerator>> getFunctions() {
    return functions.entrySet();
  }
}
