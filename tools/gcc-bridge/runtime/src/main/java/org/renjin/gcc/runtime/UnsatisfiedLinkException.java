package org.renjin.gcc.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Thrown at runtime when a function that could not be found at compile time is invoked
 */
public class UnsatisfiedLinkException extends RuntimeException {

  public UnsatisfiedLinkException(String functionName) {
    super("Function '" + functionName + "' was not found at compile-time");
  }
  
  public static void throwException(String functionName) {
    throw new UnsatisfiedLinkException(functionName);
  }
  
  public static MethodHandle throwingHandle(String functionName) throws NoSuchMethodException, IllegalAccessException {
    MethodHandle methodHandle = MethodHandles.throwException(void.class, UnsatisfiedLinkException.class);
    methodHandle = MethodHandles.insertArguments(methodHandle, 0, functionName);
    return methodHandle;
  }
}
