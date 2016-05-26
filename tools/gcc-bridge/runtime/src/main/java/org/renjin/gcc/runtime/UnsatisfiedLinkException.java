package org.renjin.gcc.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Thrown at runtime when a function that could not be found at compile time is invoked
 */
public class UnsatisfiedLinkException extends RuntimeException {

  public UnsatisfiedLinkException(String functionName) {
    super("Function '" + functionName + "' was not found at compile-time");
  }


  /**
   * Returns a method handle that throws an {@code UnsatisfiedLinkException} when invoked. 
   * 
   * <p>Used by the compiler to generate code for function pointers that reference undefined symbols.</p>
   */
  public static MethodHandle throwingHandle(String functionName) throws NoSuchMethodException, IllegalAccessException {
    MethodHandle exceptionConstructor = MethodHandles.publicLookup().findConstructor(
        UnsatisfiedLinkException.class, MethodType.methodType(void.class, String.class));

    exceptionConstructor = MethodHandles.insertArguments(exceptionConstructor, 0, functionName);
    MethodHandle exceptionThrower = MethodHandles.throwException(void.class, UnsatisfiedLinkException.class);
    
    return MethodHandles.foldArguments(exceptionThrower, exceptionConstructor);
  }
}
