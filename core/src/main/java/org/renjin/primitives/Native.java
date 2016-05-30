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
import org.renjin.invoke.reflection.FunctionBinding;
import org.renjin.methods.Methods;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

public class Native {

  public static final boolean DEBUG = false;


  public static final ThreadLocal<Context> CURRENT_CONTEXT = new ThreadLocal<>();


  @Builtin(".C")
  public static SEXP dotC(@Current Context context,
                          @Current Environment rho,
                          SEXP methodExp,
                          @ArgumentList ListVector callArguments,
                          @NamedFlag("PACKAGE") String packageName,
                          @NamedFlag("NAOK") boolean naOk,
                          @NamedFlag("DUP") boolean dup,
                          @NamedFlag("ENCODING") boolean encoding) throws IllegalAccessException {

    MethodHandle method;

    if(methodExp instanceof StringVector) {
      String methodName = ((StringVector) methodExp).getElementAsString(0);


      if(packageName.equals("base")) {
        return delegateToJavaMethod(context, Base.class, methodName, callArguments);
      }

      List<Method> methods = findMethod(getPackageClass(packageName, context), methodName);
      if (methods.isEmpty()) {
        throw new EvalException("Can't find method %s in package %s", methodName, packageName);
      }

      method = MethodHandles.publicLookup().unreflect(Iterables.getOnlyElement(methods));

    } else if(methodExp instanceof ExternalPtr && ((ExternalPtr) methodExp).getInstance() instanceof Method) {
      method = MethodHandles.publicLookup().unreflect((Method) ((ExternalPtr) methodExp).getInstance());

    } else if(methodExp instanceof ListVector) {
      ExternalPtr<MethodHandle> address = (ExternalPtr<MethodHandle>) ((ListVector)methodExp).get("address");
      method = address.getInstance();

    } else {
      throw new EvalException("Invalid method argument of type %s", methodExp.getTypeName());
    }

    Object[] nativeArguments = new Object[method.type().parameterCount()];
    for(int i=0;i!=nativeArguments.length;++i) {
      Type type = method.type().parameterType(i);
      if(type.equals(IntPtr.class)) {
        nativeArguments[i] = intPtrFromVector(callArguments.get(i));
      } else if(type.equals(DoublePtr.class)) {
        nativeArguments[i] = doublePtrFromVector(callArguments.get(i));
      } else if(type.equals(ObjectPtr.class)) {
        nativeArguments[i] = stringPtrToCharPtrPtr(callArguments.get(i));
      } else {
        throw new EvalException("Don't know how to marshall type " + callArguments.get(i).getClass().getName() +
            " to for C argument " +  type + " in call to " + method);
      }
    }

    try {
      method.invokeWithArguments(nativeArguments);
    } catch (EvalException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException(e.getMessage(), e);
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

  public static SEXP sexpFromPointer(Object ptr, AttributeMap attributes) {
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

  public static DoublePtr doublePtrFromVector(SEXP sexp) {
    if(!(sexp instanceof AtomicVector)) {
      throw new EvalException("expected atomic vector");
    }
    return new DoublePtr(((AtomicVector) sexp).toDoubleArray());
  }

  public static IntPtr intPtrFromVector(SEXP sexp) {
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

    // quick spike: fortran functions in the "base" package are all
    // defined in libappl, so point us to that class.
    // TODO: map package names to implementation classes


    MethodHandle method;
    String methodName;

    if(methodExp instanceof ListVector) {
      ListVector methodObject = (ListVector) methodExp;
      ExternalPtr<MethodHandle> address = (ExternalPtr<MethodHandle>) methodObject.get("address");
      method = address.getInstance();
      methodName = ((StringVector) methodObject.get("name")).getElementAsString(0);
    } else if(methodExp instanceof StringVector) {
      if("base".equals(packageName)) {
        className = "org.renjin.appl.Appl";
      }
      methodName = ((StringVector) methodExp).getElementAsString(0);
      method = findFortranMethod(className, methodName);

    } else if(methodExp instanceof ExternalPtr && ((ExternalPtr) methodExp).getInstance() instanceof Method) {
      Method methodRef = (Method) ((ExternalPtr) methodExp).getInstance();
      methodName = methodRef.getName();
      method =  MethodHandles.publicLookup().unreflect(methodRef);
    } else {
      throw new EvalException("Invalid argument type for method = %s", methodExp.getTypeName());
    }

    Class<?>[] fortranTypes = method.type().parameterArray();
    if(fortranTypes.length != callArguments.length()) {
      throw new EvalException("Invalid number of args");
    }

    Object[] fortranArgs = new Object[fortranTypes.length];
    ListVector.NamedBuilder returnValues = ListVector.newNamedBuilder();

    if(Profiler.ENABLED) {
      Profiler.functionStart(Symbol.get(methodName));
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
      method.invokeWithArguments(fortranArgs);
    } catch (Error e) {
      throw e;
    } catch (Throwable e) {
      throw new EvalException("Exception thrown while executing " + methodName, e);
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


  private static MethodHandle findFortranMethod(String className, String methodName) throws IllegalAccessException {
    Class<?> declaringClass = null;
    try {
      declaringClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EvalException(String.format("Could not find class named %s", className), e);
    }

    String mangledName = methodName.toLowerCase() + "_";

    for(Method method : declaringClass.getMethods()) {
      if(method.getName().equals(mangledName) &&
          Modifier.isPublic(method.getModifiers()) &&
          Modifier.isStatic(method.getModifiers())) {
        return MethodHandles.publicLookup().unreflect(method);
      }
    }
    throw new EvalException("Could not find method %s in class %s", methodName, className);
  }

  @Builtin(".Call")
  public static SEXP dotCall(@Current Context context,
                             @Current Environment rho,
                             SEXP methodExp,
                             @ArgumentList ListVector callArguments,
                             @NamedFlag("PACKAGE") String packageName,
                             @NamedFlag("CLASS") String className) throws ClassNotFoundException {

    if(methodExp.inherits("NativeSymbolInfo")) {

      ExternalPtr<MethodHandle> address = (ExternalPtr<MethodHandle>) ((ListVector)methodExp).get("address");
      MethodHandle methodHandle = address.getInstance();
      if(methodHandle.type().parameterCount() != callArguments.length()) {
        throw new EvalException("Expected %d arguments, found %d",
            methodHandle.type().parameterCount(),
            callArguments.length());
      }
      MethodHandle transformedHandle = methodHandle.asSpreader(SEXP[].class, methodHandle.type().parameterCount());
      SEXP[] arguments = toSexpArray(callArguments);
      if(Profiler.ENABLED) {
        StringVector nameExp = (StringVector)((ListVector) methodExp).get("name");
        Profiler.functionStart(Symbol.get(nameExp.getElementAsString(0)));
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

    } else if(methodExp instanceof StringVector) {

      String methodName = ((StringVector) methodExp).getElementAsString(0);

      Class clazz;
      if (packageName != null) {
        clazz = getPackageClass(packageName, context);
      } else if (className != null) {
        clazz = Class.forName(className);
      } else {
        throw new EvalException("Either the PACKAGE or CLASS argument must be provided");
      }
      if(Profiler.ENABLED) {
        Profiler.functionStart(Symbol.get(methodName));
      }
      try {
        return delegateToJavaMethod(context, clazz, methodName, callArguments);
      } finally {
        if(Profiler.ENABLED) {
          Profiler.functionEnd();
        }
      }
    } else {
      throw new EvalException("Invalid method argument: " + methodExp);
    }
  }


  @Builtin(".External")
  public static SEXP external(@Current Context context,
                              @Current Environment rho,
                              SEXP methodExp,
                              @ArgumentList ListVector callArguments,
                              @NamedFlag("PACKAGE") String packageName,
                              @NamedFlag("CLASS") String className) throws ClassNotFoundException {

    if(!methodExp.inherits("NativeSymbolInfo")) {
      throw new EvalException("Expected object of class 'NativeSymbolInfo'");
    }

    ExternalPtr<MethodHandle> address = (ExternalPtr<MethodHandle>) ((ListVector)methodExp).get("address");
    MethodHandle methodHandle = address.getInstance();
    if(methodHandle.type().parameterCount() != 1) {
      throw new EvalException("Expected method with single argument, found %d",
          methodHandle.type().parameterCount(),
          callArguments.length());
    }

    StringVector functionName = (StringVector) ((ListVector) methodExp).get("name");
    SEXP argumentList = new PairList.Node(functionName, PairList.Node.fromVector(callArguments));

    if(Profiler.ENABLED) {
      StringVector nameExp = (StringVector)((ListVector) methodExp).get("name");
      Profiler.functionStart(Symbol.get(nameExp.getElementAsString(0)));
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
                                          Class clazz,
                                          String methodName,
                                          ListVector arguments) {

    List<Method> overloads = findMethod(clazz, methodName);

    if(overloads.isEmpty()) {
      throw new EvalException("Method " + methodName + " not defined in " + clazz.getName());
    }

    FunctionBinding binding = new FunctionBinding(overloads);
    return binding.invoke(null, context, arguments);
  }

  public static List<Method> findMethod(Class packageClass, String methodName) {
    List<Method> overloads = Lists.newArrayList();
    for(Method method : packageClass.getMethods()) {
      if(method.getName().equals(methodName) &&
          (method.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) != 0) {
        overloads.add(method);
      }
    }
    return overloads;
  }

  private static Class getPackageClass(String packageName, Context context) {
    if(packageName == null || packageName.equals("base")) {
      return Base.class;
    } else if(packageName.equals("methods")) {
      return Methods.class;
    } else if(packageName.equals("grDevices")) {
      return Graphics.class;
    } else {
      Namespace namespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      FqPackageName fqname = namespace.getFullyQualifiedName();
      String packageClassName = fqname.getGroupId()+"."+fqname.getPackageName() + "." +
          fqname.getPackageName();
      try {
        return namespace.getPackage().loadClass(packageClassName);
      } catch (ClassNotFoundException e) {
        throw new EvalException("Could not load class '%s' from package '%s'", packageClassName, packageClassName);
      }
    }
  }
}