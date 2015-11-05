package org.renjin.gcc.codegen;

import org.renjin.gcc.InternalCompilerException;

public class UnimplementedException extends InternalCompilerException {
  public UnimplementedException(Class clazz, String methodName) {
    super(String.format("TODO: Implement method '%s' in class '%s'", methodName, clazz.getName()));
  }
}
