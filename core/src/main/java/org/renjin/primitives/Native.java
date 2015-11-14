package org.renjin.primitives;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.base.Base;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.gcc.runtime.BooleanPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.NamedFlag;
import org.renjin.invoke.reflection.FunctionBinding;
import org.renjin.methods.Methods;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

public class Native {

  public static final boolean DEBUG = false;


  @Builtin(".C")
  public static SEXP dotC(@Current Context context,
                          @Current Environment rho,
                          SEXP methodExp,
                          @ArgumentList ListVector callArguments,
                          @NamedFlag("PACKAGE") String packageName,
                          @NamedFlag("NAOK") boolean naOk,
                          @NamedFlag("DUP") boolean dup,
                          @NamedFlag("ENCODING") boolean encoding) {

    Method method;

    if(methodExp instanceof StringVector) {
      String methodName = ((StringVector) methodExp).getElementAsString(0);


      if(packageName.equals("base")) {
        return delegateToJavaMethod(context, Base.class, methodName, callArguments);
      }

      List<Method> methods = findMethod(getPackageClass(packageName, context), methodName);
      if (methods.isEmpty()) {
         throw new EvalException("Can't find method %s in package %s", methodName, packageName);
      } 

      method = Iterables.getOnlyElement(methods);

    } else if(methodExp instanceof ExternalPtr && ((ExternalPtr) methodExp).getInstance() instanceof Method) {
      method = (Method) ((ExternalPtr) methodExp).getInstance();

    } else {
      throw new EvalException("Invalid method argument of type %s", methodExp.getTypeName());
    }

    Object[] nativeArguments = new Object[method.getParameterTypes().length];
    for(int i=0;i!=nativeArguments.length;++i) {
      Type type = method.getParameterTypes()[i];
      if(type.equals(IntPtr.class)) {
        nativeArguments[i] = intPtrFromVector(callArguments.get(i));
      } else if(type.equals(DoublePtr.class)) {
        nativeArguments[i] = doublePtrFromVector(callArguments.get(i));
      } else {
         throw new EvalException("Don't know how to marshall type " + callArguments.get(i).getClass().getName() +
                 " to for C argument " +  type + " in call to " + method.getName());
      }
    }

    try {
      method.invoke(null, nativeArguments);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      throw new EvalException(e.getCause().getMessage(), e.getCause());
    } catch (Exception e) {
      throw new EvalException(e);
    }

    ListVector.NamedBuilder builder = new ListVector.NamedBuilder();
    for(int i=0;i!=nativeArguments.length;++i) {
      if(DEBUG) {
        java.lang.System.out.println(callArguments.getName(i) + " = " + nativeArguments[i].toString());
      }
      builder.add(callArguments.getName(i), sexpFromPointer(nativeArguments[i]));
    }
    return builder.build();
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

  public static SEXP sexpFromPointer(Object ptr) {
    // Currently, our GCC bridge doesn't support storing values
    // to fields, so we can be confident that no other references
    // to these pointers exist
    if(ptr instanceof DoublePtr) {
      return DoubleArrayVector.unsafe(((DoublePtr) ptr).array);
    } else if(ptr instanceof IntPtr) {
      return new IntArrayVector(((IntPtr) ptr).array);
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
                          @NamedFlag("ENCODING") boolean encoding) {

    // quick spike: fortran functions in the "base" package are all
    // defined in libappl, so point us to that class.
    // TODO: map package names to implementation classes


    Method method;
    if(methodExp instanceof StringVector) {
      if("base".equals(packageName)) {
        className = "org.renjin.appl.Appl";
      }
      String methodName = ((StringVector) methodExp).getElementAsString(0);
      method = findFortranMethod(className, methodName);

    } else if(methodExp instanceof ExternalPtr && ((ExternalPtr) methodExp).getInstance() instanceof Method) {
      method = (Method) ((ExternalPtr) methodExp).getInstance();
    } else {
      throw new EvalException("Invalid argument type for method = %s", methodExp.getTypeName());
    }

    Class<?>[] fortranTypes = method.getParameterTypes();
    if(fortranTypes.length != callArguments.length()) {
      throw new EvalException("Invalid number of args");
    }

    Object[] fortranArgs = new Object[fortranTypes.length];
    ListVector.NamedBuilder returnValues = ListVector.newNamedBuilder();

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
      method.invoke(null, fortranArgs);
    } catch (Exception e) {
      throw new EvalException("Exception thrown while executing " + method.getName(), e);
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


  private static Method findFortranMethod(String className, String methodName) {
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
        return method;
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
      try {
        if (methodHandle.type().returnType().equals(void.class)) {
          transformedHandle.invokeExact(arguments);
          return Null.INSTANCE;
        } else {
          return (SEXP) transformedHandle.invokeExact(arguments);
        }
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        throw new EvalException("Exception calling " + methodExp, e);
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

      return delegateToJavaMethod(context, clazz, methodName, callArguments);
    } else {
      throw new EvalException("Invalid method argument: " + methodExp);
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
      Namespace namespace = context.getNamespaceRegistry().getNamespace(packageName);
      FqPackageName fqname = namespace.getFullyQualifiedName();
      String packageClassName = fqname.getGroupId()+"."+fqname.getPackageName() + "." +
                                fqname.getPackageName();
      return namespace.getPackage().loadClass(packageClassName);
    }
  }
}
