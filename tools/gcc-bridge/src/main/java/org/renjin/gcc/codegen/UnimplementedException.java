package org.renjin.gcc.codegen;

import org.renjin.gcc.InternalCompilerException;

/**
 * Thrown when a part of a {@link org.renjin.gcc.codegen.type.TypeStrategy} is not yet implemented.
 */
public class UnimplementedException extends InternalCompilerException {
  public UnimplementedException(Class clazz, String methodName) {
    super(String.format("TODO: Implement method '%s' in class '%s'", methodName, clazz.getName()));
  }
}
