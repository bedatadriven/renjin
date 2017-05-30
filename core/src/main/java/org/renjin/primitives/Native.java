/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives;

import org.renjin.base.Base;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Profiler;
import org.renjin.gcc.runtime.*;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.NamedFlag;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.invoke.reflection.FunctionBinding;
import org.renjin.methods.Methods;
import org.renjin.primitives.packaging.DllSymbol;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

public class Native {

  public static final boolean DEBUG = false;


  public static final ThreadLocal<Context> CURRENT_CONTEXT = new ThreadLocal<>();

  public static Context currentContext() {
    Context context = Native.CURRENT_CONTEXT.get();
    if(context == null) {
      throw new IllegalStateException("Renjin context not initialized for this thread.");
    }
    return context;
  }

  @Builtin(".C")
  public static SEXP dotC(@Current Context context,
                          @Current Environment rho,
                          SEXP methodExp,
                          @ArgumentList ListVector callArguments,
                          @NamedFlag("PACKAGE") String packageName,
                          @NamedFlag("NAOK") boolean naOk,
                          @NamedFlag("DUP") boolean dup,
                          @NamedFlag("COPY") boolean copy,
                          @NamedFlag("ENCODING") boolean encoding) throws IllegalAccessException {


    if("base".equals(packageName)) {
      return delegateToJavaMethod(context, methodExp, packageName, null, callArguments);
    }

    DllSymbol method = findMethod(context, methodExp, packageName, null, DllSymbol.Convention.C);
    MethodHandle handle = method.getMethodHandle();

    Object[] nativeArguments = new Object[handle.type().parameterCount()];
    for(int i=0;i!=nativeArguments.length;++i) {
      Type type = handle.type().parameterType(i);
      if(type.equals(IntPtr.class)) {
        nativeArguments[i] = intPtrFromVector(callArguments.get(i));
      } else if(type.equals(DoublePtr.class)) {
        nativeArguments[i] = doublePtrFromVector(callArguments.get(i));
      } else if(type.equals(ObjectPtr.class)) {
        nativeArguments[i] = stringPtrToCharPtrPtr(callArguments.get(i));
      } else {
        throw new EvalException("Don't know how to marshall type " + callArguments.get(i).getClass().getName() +
            " to for C argument " +  type + " in call to " + handle);
      }
    }
    
    if(Profiler.ENABLED) {
      Profiler.functionStart(Symbol.get(method.getName()), 'C');
    }

    try {
      handle.invokeWithArguments(nativeArguments);
    } catch (EvalException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException(e.getMessage(), e);
    } finally {
      if(Profiler.ENABLED) {
        Profiler.functionEnd();
      }
    }

    ListVector.NamedBuilder builder = new ListVector.NamedBuilder();
    for(int i=0;i!=nativeArguments.length;++i) {
      if(DEBUG) {
        java.lang.System.out.println(callArguments.getName(i) + " = " + nativeArguments[i].toString());
      }
      builder.add(callArguments.getName(i), sexpFromPointer(
          nativeArguments[i],
          callArguments.get(i).getAttributes()));
    }
    return builder.build();
  }

  /**
   * Converts a StringVector to an array of null-terminated strings.
   */
  private static ObjectPtr stringPtrToCharPtrPtr(SEXP sexp) {
    if(!((sexp instanceof StringVector))) {
      throw new EvalException(".C function expected 'character', but argument was '%s'", sexp.getTypeName());
    }
    StringVector vector = (StringVector) sexp;
    BytePtr[] strings = new BytePtr[sexp.length()];
    for(int i=0;i<sexp.length();++i) {
      String element = vector.getElementAsString(i);
      if(element != null) {
        strings[i] = BytePtr.nullTerminatedString(element, Charsets.UTF_8);
      }
    }
    return new ObjectPtr(strings, 0);
  }

  private static void dumpCall(String methodName, String packageName, ListVector callArguments) {
    java.lang.System.out.print(".C('" + methodName + "', ");
    for(NamedValue arg : callArguments.namedValues()) {
      if(!Strings.isNullOrEmpty(arg.getName())) {
        java.lang.System.out.print(arg.getName() + " = ");
      }
      java.lang.System.out.println(Deparse.deparse(null, arg.getValue(), 80, false, 0, 0) + ", ");
    }
    java.lang.System.out.println("PACKAGE = '" + packageName + "')");
  }

  private static SEXP sexpFromPointer(Object ptr, AttributeMap attributes) {
    // We are trusting the C code not to modify the arrays after the call
    // returns. 
    if(ptr instanceof DoublePtr) {
      return DoubleArrayVector.unsafe(((DoublePtr) ptr).array, attributes);
    } else if(ptr instanceof IntPtr) {
      return new IntArrayVector(((IntPtr) ptr).array, attributes);
    } else if(ptr instanceof ObjectPtr) {
      return new NativeStringVector((ObjectPtr)ptr, attributes);
    } else {
      throw new UnsupportedOperationException(ptr.toString());
    }
  }

  private static DoublePtr doublePtrFromVector(SEXP sexp) {
    if(!(sexp instanceof AtomicVector)) {
      throw new EvalException("expected atomic vector");
    }
    return new DoublePtr(((AtomicVector) sexp).toDoubleArray());
  }

  private static IntPtr intPtrFromVector(SEXP sexp) {
    if(!(sexp instanceof AtomicVector)) {
      throw new EvalException("expected atomic vector");
    }
    AtomicVector vector = (AtomicVector)sexp;
    int[] array = new int[vector.length()];
    for(int i=0;i!=array.length;++i) {
      array[i] = vector.getElementAsInt(i);
    }
    return new IntPtr(array, 0);
  }

  /**
   * Invokes a method compiled to JVM byte code from Fortran, applying the correct calling
   * conventions, etc. This method differs from the
   */
  @Builtin(".Fortran")
  public static SEXP dotFortran(@Current Context context,
                                @Current Environment rho,
                                SEXP methodExp,
                                @ArgumentList ListVector callArguments,
                                @NamedFlag("PACKAGE") String packageName,
                                @NamedFlag("CLASS") String className,
                                @NamedFlag("NAOK") boolean naOk,
                                @NamedFlag("DUP") boolean dup,
                                @NamedFlag("ENCODING") boolean encoding) throws IllegalAccessException {

    DllSymbol method = findMethod(context, methodExp, packageName, className, DllSymbol.Convention.FORTRAN);

    Class<?>[] fortranTypes = method.getMethodHandle().type().parameterArray();
    if(fortranTypes.length != callArguments.length()) {
      throw new EvalException("Invalid number of args");
    }

    Object[] fortranArgs = new Object[fortranTypes.length];
    ListVector.NamedBuilder returnValues = ListVector.newNamedBuilder();

    if(Profiler.ENABLED) {
      Profiler.functionStart(Symbol.get(method.getName()), 'F');
    }

    // For .Fortran() calls, we make a copy of the arguments, pass them by
    // reference to the fortran subroutine, and then return the modified arguments
    // as a ListVector.

    for(int i=0;i!=callArguments.length();++i) {
      AtomicVector vector = (AtomicVector) callArguments.get(i);
      if(fortranTypes[i].equals(DoublePtr.class)) {
        double[] array = vector.toDoubleArray();
        fortranArgs[i] = new DoublePtr(array, 0);
        returnValues.add(callArguments.getName(i), DoubleArrayVector.unsafe(array, vector.getAttributes()));

      } else if(fortranTypes[i].equals(IntPtr.class)) {
        int[] array = vector.toIntArray();
        fortranArgs[i] = new IntPtr(array, 0);
        returnValues.add(callArguments.getName(i), IntArrayVector.unsafe(array, vector.getAttributes()));

      } else if(fortranTypes[i].equals(BooleanPtr.class)) {
        boolean[] array = toBooleanArray(vector);
        fortranArgs[i] = new BooleanPtr(array);
        returnValues.add(callArguments.getName(i), BooleanArrayVector.unsafe(array));

      } else {
        throw new UnsupportedOperationException("fortran type: " + fortranTypes[i]);
      }
    }

    try {
      method.getMethodHandle().invokeWithArguments(fortranArgs);
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception thrown while executing " + method.getName(), e);
    } finally {
      if(Profiler.ENABLED) {
        Profiler.functionEnd();
      }
    }

    return returnValues.build();
  }

  private static boolean[] toBooleanArray(AtomicVector vector) {
    boolean array[] = new boolean[vector.length()];
    for(int i=0;i<vector.length();++i) {
      int element = vector.getElementAsRawLogical(i);
      if(element == IntVector.NA) {
        throw new EvalException("NAs cannot be passed to logical fortran argument");
      }
      array[i] = (element != 0);
    }
    return array;
  }

  @Builtin(".Call")
  public static SEXP redotCall(@Current Context context,
                             @Current Environment rho,
                             SEXP methodExp,
                             @ArgumentList ListVector callArguments,
                             @NamedFlag("PACKAGE") String packageName,
                             @NamedFlag("COPY") boolean copy,
                             @NamedFlag("CLASSES") StringVector classes,
                             @NamedFlag("CLASS") String className) throws ClassNotFoundException {


    if("base".equals(packageName) || "methods".equals(packageName) || className != null) {
      return delegateToJavaMethod(context, methodExp, packageName, className, callArguments);
    }

    DllSymbol method = findMethod(context, methodExp, packageName, className, DllSymbol.Convention.CALL);

    MethodHandle methodHandle = method.getMethodHandle();
    if(methodHandle.type().parameterCount() != callArguments.length()) {
      throw new EvalException("Expected %d arguments, found %d",
          methodHandle.type().parameterCount(),
          callArguments.length());
    }
    MethodHandle transformedHandle = methodHandle.asSpreader(SEXP[].class, methodHandle.type().parameterCount());
    SEXP[] arguments = toSexpArray(callArguments);
    if(Profiler.ENABLED) {
      StringVector nameExp = (StringVector)((ListVector) methodExp).get("name");
      Profiler.functionStart(Symbol.get(nameExp.getElementAsString(0)), 'C');
    }
    Context previousContext = CURRENT_CONTEXT.get();
    try {
      CURRENT_CONTEXT.set(context);
      if (methodHandle.type().returnType().equals(void.class)) {
        transformedHandle.invokeExact(arguments);
        return Null.INSTANCE;
      } else {
        return (SEXP) transformedHandle.invokeExact(arguments);
      }
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception calling " + methodExp + " : " + e.getMessage(), e);
    } finally {
      CURRENT_CONTEXT.set(previousContext);
      if(Profiler.ENABLED) {
        Profiler.functionEnd();
      }
    }
  }


  @Builtin(".External")
  public static SEXP external(@Current Context context,
                              @Current Environment rho,
                              SEXP methodExp,
                              @ArgumentList ListVector callArguments,
                              @NamedFlag("PACKAGE") String packageName,
                              @NamedFlag("CLASS") String className) throws ClassNotFoundException {


    DllSymbol symbol = findMethod(context, methodExp, packageName, className, DllSymbol.Convention.EXTERNAL);

    MethodHandle methodHandle = symbol.getMethodHandle();
    if(methodHandle.type().parameterCount() != 1) {
      throw new EvalException("Expected method with single argument, found %d",
          methodHandle.type().parameterCount(),
          callArguments.length());
    }

    SEXP argumentList = new PairList.Node(StringVector.valueOf(symbol.getName()), PairList.Node.fromVector(callArguments));

    if(Profiler.ENABLED) {
      StringVector nameExp = (StringVector)((ListVector) methodExp).get("name");
      Profiler.functionStart(Symbol.get(nameExp.getElementAsString(0)), 'C');
    }
    Context previousContext = CURRENT_CONTEXT.get();
    try {
      CURRENT_CONTEXT.set(context);
      if (methodHandle.type().returnType().equals(void.class)) {
        methodHandle.invokeExact(argumentList);
        return Null.INSTANCE;
      } else {
        return (SEXP) methodHandle.invokeExact(argumentList);
      }
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception calling " + methodExp + " : " + e.getMessage(), e);
    } finally {
      CURRENT_CONTEXT.set(previousContext);
      if(Profiler.ENABLED) {
        Profiler.functionEnd();
      }
    }
  }

  private static SEXP[] toSexpArray(ListVector callArguments) {
    SEXP args[] = new SEXP[callArguments.length()];
    for (int i = 0; i < callArguments.length(); i++) {
      args[i] = callArguments.get(i);
    }
    return args;
  }

  /**
   * Dispatches what were originally calls to "native" libraries (C/Fortran/etc)
   * to a Java class. The Calling convention (.C/.Fortran/.Call) are ignored.
   *
   */
  public static SEXP delegateToJavaMethod(Context context,
                                          SEXP method, String packageName,
                                          String className,
                                          ListVector arguments) {

    Class declaringClass;
    if("base".equals(packageName)) {
      declaringClass = Base.class;
    } else if("methods".equals(packageName)) {
      declaringClass = Methods.class;
    } else {
      try {
        declaringClass = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new EvalException("Cannot find JVM class " + className);
      }
    }

    ClassBindingImpl classBinding = ClassBindingImpl.get(declaringClass);
    FunctionBinding functionBinding = classBinding.getStaticMethodBinding(method.asString());

    return functionBinding.invoke(null, context, arguments);
  }

  private static DllSymbol findMethod(Context context, SEXP method, String packageName, String className, DllSymbol.Convention convention) {

    if(method.inherits("NativeSymbolInfo")) {
      return DllSymbol.fromSexp(method);
    }

    if(method instanceof ExternalPtr) {
      return findMethodFromExternalPointer((ExternalPtr<?>) method);
    }

    if(method instanceof StringVector) {
      return findMethodByName(context, method.asString(), packageName, className, convention);
    }

    throw new EvalException("Invalid method object of type '%s'", method.getTypeName());
  }

  private static DllSymbol findMethodFromExternalPointer(ExternalPtr<?> method)  {
    if (method.getInstance() instanceof Method) {
      return new DllSymbol((Method) method.getInstance());
    }
    throw new EvalException("Invalid method external pointer of (java) class '%s'", method.getInstance().getClass().getName());
  }

  private static DllSymbol findMethodByName(Context context, String methodName, String packageName, String className, DllSymbol.Convention convention) {

    if(convention == DllSymbol.Convention.FORTRAN) {
      methodName = methodName + "_";
    }

    if(convention == DllSymbol.Convention.FORTRAN && "base".equals(packageName)) {
      return findMethodByReflection(methodName, "org.renjin.appl.Appl");
    }

    if(className != null) {
      return findMethodByReflection(methodName, className);
    }

    if(packageName == null) {
      return findGlobalMethodByName(context, methodName);

    } else {
      Namespace namespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      DllSymbol symbol = namespace.getNativeSymbolMap().get(methodName);

      if(symbol == null) {
        // Special case...
        // This should no longer be necessary if/when we update to latest version of GNU R stats.
        if("stats".equals(packageName)) {
          return findMethodByReflection(methodName, "org.renjin.stats.stats");
        }
      }

      if(symbol == null) {
        throw new EvalException("Could not resolve native method '%s' in package '%s'", methodName, packageName);
      }

      return symbol;
    }
  }

  private static DllSymbol findMethodByReflection(String methodName, String className) {
    Class<?> declaringClass = null;
    try {
      declaringClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EvalException("Could not find Java class " + className);
    }

    return findMethodByReflection(methodName, declaringClass);
  }

  private static DllSymbol findMethodByReflection(String methodName, Class<?> declaringClass) {
    for(Method method : declaringClass.getMethods()) {
      if(method.getName().equals(methodName) &&
          Modifier.isPublic(method.getModifiers()) &&
          Modifier.isStatic(method.getModifiers())) {


        return new DllSymbol(method);
      }
    }

    throw new EvalException("Could not find method %s in class %s", methodName, declaringClass.getName());
  }

  /**
   * When only a method name is provided without a package, we have to look for a native symbol in
   * the global lookup.
   */
  private static DllSymbol findGlobalMethodByName(Context context, String methodName) {
    Optional<DllSymbol> symbol = context.getNamespaceRegistry().resolveNativeMethod(methodName);
    if(symbol.isPresent()) {
      return symbol.get();
    } else {
      throw new EvalException("Could not resolve native method '%s'", methodName);
    }
  }

}