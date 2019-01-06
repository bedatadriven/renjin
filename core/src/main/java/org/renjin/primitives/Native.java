/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.PointerPtr;
import org.renjin.invoke.annotations.*;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.invoke.reflection.FunctionBinding;
import org.renjin.methods.Methods;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.DllSymbol;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

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

  @Internal
  public static ListVector getLoadedDLLs(@Current Context context) {

    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    list.setAttribute(Symbols.CLASS, StringVector.valueOf("DLLInfoList"));

    for (DllInfo dllInfo : context.getSession().getLoadedLibraries()) {
      list.add(dllInfo.getLibraryName(), dllInfo.buildDllInfoSexp());
    }

    return list.build();
  }

  @Internal("is.loaded")
  public static boolean isLoaded(@Current Context context, String symbol, String packageName, String type) {
    Iterable<DllInfo> libraries;
    if(Strings.isNullOrEmpty(packageName)) {
      libraries = context.getSession().getLoadedLibraries();
    } else {
      Optional<Namespace> namespace = context.getSession().getNamespaceRegistry().getNamespaceIfPresent(Symbol.get(symbol));
      if(namespace.isPresent()) {
        libraries = namespace.get().getLibraries();
      } else {
        libraries = Collections.emptySet();
      }
    }

    Predicate<DllSymbol> predicate;
    if("Fortran".equals(type)) {
      predicate = (x -> x.getConvention() == DllSymbol.Convention.FORTRAN);
    } else if("Call".equals(type)) {
      predicate = (x -> x.getConvention() == DllSymbol.Convention.CALL);
    } else if("External".equals(type)) {
      predicate = (x -> x.getConvention() == DllSymbol.Convention.EXTERNAL);
    } else {
      predicate = (x -> true);
    }

    for (DllInfo library : libraries) {
      if(library.isLoaded(symbol, predicate)) {
        return true;
      }
    }
    return false;
  }

  @Internal
  public static ListVector getRegisteredRoutines(DllInfo dllInfo) {
    return dllInfo.buildRegisteredRoutinesSexp();
  }

  @Internal
  public static ListVector getSymbolInfo(@Current Context context, String name, String packageName, boolean withRegistrationInfo) {

    if(packageName.isEmpty()) {
      for (DllInfo dllInfo : context.getSession().getLoadedLibraries()) {
        Optional<DllSymbol> symbol = dllInfo.getSymbol(name);
        if(symbol.isPresent()) {
          return symbol.get().buildNativeSymbolInfoSexp();
        }
      }
      throw new EvalException("No such symbol " +  name);

    } else {

      Optional<Namespace> namespace = context.getNamespaceRegistry().getNamespaceIfPresent(Symbol.get(packageName));
      if(namespace.isPresent()) {
        for (DllInfo dllInfo : namespace.get().getLibraries()) {
          Optional<DllSymbol> symbol = dllInfo.getSymbol(name);
          if(symbol.isPresent()) {
            return symbol.get().buildNativeSymbolInfoSexp();
          }
        }
      }

      throw new EvalException("No such symbol " + name + " in package " + packageName);
    }
  }

  @Internal
  public static ListVector getSymbolInfo(String name, DllInfo dllInfo, boolean withRegistrationInfo) {
    Optional<DllSymbol> registeredSymbol = dllInfo.getRegisteredSymbol(name);
    if(registeredSymbol.isPresent()) {
      return registeredSymbol.get().buildNativeSymbolInfoSexp();
    }

    throw new EvalException("No such symbol " + name + " in library " + dllInfo.getLibraryName());
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
      SEXP callArgument = callArguments.get(i);
      if(callArgument instanceof IntVector || callArgument instanceof LogicalVector) {
        nativeArguments[i] = intPtrFromVector(callArguments.get(i));
      } else if(callArgument instanceof DoubleVector) {
        nativeArguments[i] = doublePtrFromVector(callArguments.get(i));
      } else if(callArgument instanceof StringVector) {
        nativeArguments[i] = stringPtrToCharPtrPtr(callArguments.get(i));
      } else {
        throw new EvalException("Don't know how to marshall type " + callArguments.get(i).getClass().getName() +
            " to for C argument " +  type + " in call to " + handle);
      }
    }
    
    if(Profiler.ENABLED) {
      Profiler.functionStart(Symbol.get(method.getName()), 'C');
    }
    Context previousContext = CURRENT_CONTEXT.get();
    CURRENT_CONTEXT.set(context);
    try {
      handle.invokeWithArguments(nativeArguments);
    } catch (EvalException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException(e.getMessage(), e);
    } finally {
      CURRENT_CONTEXT.set(previousContext);
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
          callArguments.get(i)));
    }
    return builder.build();
  }

  /**
   * Converts a StringVector to an array of null-terminated strings.
   */
  private static PointerPtr stringPtrToCharPtrPtr(SEXP sexp) {
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
    return new PointerPtr(strings, 0);
  }

  private static SEXP sexpFromPointer(Object ptr, SEXP inputArgument) {
    // We are trusting the C code not to modify the arrays after the call
    // returns. 
    if(ptr instanceof DoublePtr) {
      return DoubleArrayVector.unsafe(((DoublePtr) ptr).array, inputArgument.getAttributes());
    } else if(ptr instanceof IntPtr) {
      return new IntArrayVector(((IntPtr) ptr).array, inputArgument.getAttributes());
    } else if(ptr instanceof PointerPtr) {
      return new NativeStringVector((PointerPtr) ptr, inputArgument.getAttributes());
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
    if(fortranTypes.length > callArguments.length()) {
      throw new EvalException("Argument mismatch while invoking .Fortran(" + method.getName() + ", ...): " +
          " expected " + fortranTypes.length + " arguments, received " + callArguments.length() + " arguments");
    }

    Object[] fortranArgs = new Object[fortranTypes.length];
    ListVector.NamedBuilder returnValues = ListVector.newNamedBuilder();

    if(Profiler.ENABLED) {
      Profiler.functionStart(Symbol.get(method.getName()), 'F');
    }

    // For .Fortran() calls, we make a copy of the arguments, pass them by
    // reference to the fortran subroutine, and then return the modified arguments
    // as a ListVector.

    for(int i=0;i!=fortranTypes.length;++i) {
      AtomicVector vector = (AtomicVector) callArguments.get(i);
      if(vector instanceof DoubleVector) {
        double[] array = vector.toDoubleArray();
        fortranArgs[i] = new DoublePtr(array, 0);
        returnValues.add(callArguments.getName(i), DoubleArrayVector.unsafe(array, vector.getAttributes()));

      } else if(vector instanceof IntVector || vector instanceof LogicalVector) {
        int[] array = vector.toIntArray();
        fortranArgs[i] = new IntPtr(array, 0);
        returnValues.add(callArguments.getName(i), IntArrayVector.unsafe(array, vector.getAttributes()));

      } else {
        throw new UnsupportedOperationException("fortran type: " + vector.getTypeName());
      }
    }

    Context previousContext = CURRENT_CONTEXT.get();
    CURRENT_CONTEXT.set(context);
    try {
      method.getMethodHandle().invokeWithArguments(fortranArgs);
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception thrown while executing " + method.getName(), e);
    } finally {
      CURRENT_CONTEXT.set(previousContext);
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
    if(methodHandle == null) {
      throw new NullPointerException("methodHandle for " + method.getName() + " is null.");
    }
    if(methodHandle.type().parameterCount() != callArguments.length()) {
      throw new EvalException("Expected %d arguments, found %d in call to %s",
          methodHandle.type().parameterCount(),
          callArguments.length(),
          method.getName());
    }
    MethodHandle transformedHandle = methodHandle.asSpreader(SEXP[].class, methodHandle.type().parameterCount());
    SEXP[] arguments = toSexpArray(callArguments);
    if(Profiler.ENABLED) {
      Profiler.functionStart(Symbol.get(method.getName()), 'C');
    }
    Context previousContext = CURRENT_CONTEXT.get();
    try {
      CURRENT_CONTEXT.set(context);
      if (transformedHandle.type().returnType().equals(void.class)) {
        transformedHandle.invokeExact(arguments);
        return Null.INSTANCE;
      } else {
        SEXP result = (SEXP) transformedHandle.invokeExact(arguments);
        return result;
      }
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception calling " +  method.getName() + " : " + e.getMessage(), e);
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
                              @NamedFlag("CLASS") String className) {


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

  @Builtin(".External2")
  public static SEXP external2(@Current Context context,
                              SEXP methodExp,
                              @ArgumentList ListVector callArguments,
                              @NamedFlag("PACKAGE") String packageName,
                              @NamedFlag("CLASS") String className) throws ClassNotFoundException {


    DllSymbol symbol = findMethod(context, methodExp, packageName, className, DllSymbol.Convention.EXTERNAL);

    MethodHandle methodHandle = symbol.getMethodHandle();
    if(methodHandle.type().parameterCount() != 4) {
      throw new EvalException("Expected method with four arguments, found %d",
          methodHandle.type().parameterCount(),
          callArguments.length());
    }

    SEXP call = context.getCall();
    SEXP op = Primitives.getPrimitive(Symbol.get(".External2"));
    SEXP args = new PairList.Node(methodExp, PairList.Node.fromVector(callArguments));
    SEXP rho = context.getEnvironment();

    if(Profiler.ENABLED) {
      StringVector nameExp = (StringVector)((ListVector) methodExp).get("name");
      Profiler.functionStart(Symbol.get(nameExp.getElementAsString(0)), 'C');
    }
    Context previousContext = CURRENT_CONTEXT.get();
    try {
      CURRENT_CONTEXT.set(context);
      if (methodHandle.type().returnType().equals(void.class)) {
        methodHandle.invokeExact(call, op, args, rho);
        return Null.INSTANCE;
      } else {
        return (SEXP) methodHandle.invokeExact(call, op, args, rho);
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

    if(method.inherits("NativeSymbol") || method.inherits("RegisteredNativeSymbol")) {
      return DllSymbol.fromAddressSexp(method);
    }

    if(method instanceof ExternalPtr) {
      return findMethodFromExternalPointer(convention, (ExternalPtr<?>) method);
    }

    if(method instanceof StringVector) {
      return findMethodByName(context, method.asString(), packageName, className, convention);
    }

    throw new EvalException("Invalid method object of type '%s'", method.getTypeName());
  }

  private static DllSymbol findMethodFromExternalPointer(DllSymbol.Convention convention, ExternalPtr<?> method)  {
    if (method.getInstance() instanceof Method) {
      return new DllSymbol(convention, (Method) method.getInstance());
    }
    throw new EvalException("Invalid method external pointer of (java) class '%s'", method.getInstance().getClass().getName());
  }

  private static DllSymbol findMethodByName(Context context, String methodName, String packageName, String className, DllSymbol.Convention convention) {

    if(className != null) {
      return findMethodByReflection(methodName, convention, className);
    }

    if(packageName == null) {
      return findGlobalMethodByName(context, convention, methodName);

    } else {
      Namespace namespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      Optional<DllSymbol> symbol = namespace.lookupSymbol(convention, methodName);

      if(!symbol.isPresent()) {
        throw new EvalException("Could not resolve native method '%s' in package '%s'", methodName, packageName);
      }

      return symbol.get();
    }
  }

  private static DllSymbol findMethodByReflection(String methodName, DllSymbol.Convention convention, String className) {
    Class<?> declaringClass = null;
    try {
      declaringClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EvalException("Could not find Java class " + className);
    }

    return findMethodByReflection(methodName, convention, declaringClass);
  }

  private static DllSymbol findMethodByReflection(String methodName, DllSymbol.Convention convention, Class<?> declaringClass) {
    for(Method method : declaringClass.getMethods()) {
      if(method.getName().equals(methodName) &&
          Modifier.isPublic(method.getModifiers()) &&
          Modifier.isStatic(method.getModifiers())) {


        return new DllSymbol(convention, method);
      }
    }

    throw new EvalException("Could not find method %s in class %s", methodName, declaringClass.getName());
  }

  /**
   * When only a method name is provided without a package, we have to look for a native symbol in
   * the global lookup.
   */
  private static DllSymbol findGlobalMethodByName(Context context, DllSymbol.Convention convention, String methodName) {

    for (DllInfo library : context.getSession().getLoadedLibraries()) {
      Optional<DllSymbol> symbol = library.lookup(convention, methodName);
      if(symbol.isPresent()) {
        return symbol.get();
      }
    }
    throw new EvalException("Could not resolve native method '%s'", methodName);
  }
}