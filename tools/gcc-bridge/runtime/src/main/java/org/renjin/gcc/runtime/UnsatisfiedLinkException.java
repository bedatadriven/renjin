/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
