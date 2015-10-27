package org.renjin.gcc.codegen.call;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.PrimitiveParamGenerator;
import org.renjin.gcc.codegen.param.PrimitivePtrParamGenerator;
import org.renjin.gcc.codegen.param.StringParamGenerator;
import org.renjin.gcc.codegen.ret.PrimitiveReturnGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.ret.VoidReturnGenerator;
import org.renjin.gcc.gimple.CallingConvention;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.runtime.Builtins;
import org.renjin.gcc.runtime.CharPtr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Provides a mapping from function names to {@code CallGenerator}s
 */
public class FunctionTable {
  
  private Map<String, MethodCallGenerator> functions = new HashMap<String, MethodCallGenerator>();


  public CallGenerator find(GimpleFunctionRef ref, CallingConvention callingConvention) {
    String mangledName = callingConvention.mangleFunctionName(ref.getName());
    MethodCallGenerator generator = functions.get(mangledName);
    if(generator == null) {
      throw new UnsupportedOperationException("Could not find function '" + mangledName + "'");
    }
    return generator;
  }
  
  public Handle findHandle(GimpleFunctionRef ref, CallingConvention callingConvention) {
    MethodCallGenerator methodCallGenerator = (MethodCallGenerator) find(ref, callingConvention);
    return methodCallGenerator.getHandle();
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
    MethodCallGenerator generator = new MethodCallGenerator(className, 
        function.getMangledName(),
        function.getParamGenerators(),
        function.getReturnGenerator());

    functions.put(function.getMangledName(), generator);
  }
  
  public void add(String functionName, Method method) {
    Preconditions.checkArgument(Modifier.isStatic(method.getModifiers()), "Method '%s' must be static", method);

    MethodCallGenerator generator = new MethodCallGenerator(
        Type.getInternalName(method.getDeclaringClass()),
        method.getName(),
        createParamGenerators(method),
        createReturnGenerator(method));
    
    functions.put(functionName, generator);
  }

  public void addMethods(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if(Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) {
        add(method.getName(), method);
      }
    }
  }



  /**
   * Creates a list of {@code ParamGenerators} from an existing JVM method.
   * 
   * <p>Note that there is not a one-to-one relationship between JVM method parameters and
   * our {@code ParamGenerators}; a complex pointer is represented as a {@code double[]} and an 
   * {@code int} offset, for example.</p>
   */
  private List<ParamGenerator> createParamGenerators(Method method) {

    List<ParamGenerator> generators = new ArrayList<ParamGenerator>();

    int index = 0;
    while(index < method.getParameterTypes().length) {
      Class<?> paramClass = method.getParameterTypes()[index];
      try {
        if (WrapperType.is(paramClass) && !paramClass.equals(CharPtr.class)) {
          WrapperType wrapperType = WrapperType.valueOf(paramClass);
          generators.add(new PrimitivePtrParamGenerator(wrapperType.getGimpleType()));
          index++;

        } else if (paramClass.isPrimitive()) {
          generators.add(new PrimitiveParamGenerator(GimplePrimitiveType.fromJvmType(paramClass)));
          index++;

        } else if (paramClass.equals(String.class)) {
          generators.add(new StringParamGenerator());
          index++;

        } else {
          throw new UnsupportedOperationException(String.format(
              "Unsupported parameter %d of type %s in method %s.%s",
              index,
              paramClass,
              method.getDeclaringClass().getName(), method.getName()));
        }
      } catch (Exception e) {
        throw new InternalCompilerException(String.format(
            "Exception creating ParamGenerator for parameter %d of type %s in method %s.%s",
            index,
            paramClass,
            method.getDeclaringClass().getName(), method.getName()));      
      }
    }
    return generators;
  }

  private ReturnGenerator createReturnGenerator(Method method) {

    Class<?> returnType = method.getReturnType();
    if(returnType.equals(void.class)) {
      return new VoidReturnGenerator();
    
    } else if(returnType.isPrimitive()) {
      return new PrimitiveReturnGenerator(GimplePrimitiveType.fromJvmType(returnType));
  
    } else {
      throw new UnsupportedOperationException(String.format(
          "Unsupported return type %s in method %s.%s",
          returnType.getName(),
          method.getDeclaringClass().getName(), method.getName()));
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
