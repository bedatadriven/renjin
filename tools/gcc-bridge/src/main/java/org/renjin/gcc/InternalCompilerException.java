package org.renjin.gcc;

import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.gimple.GimpleFunction;

/**
 * Exception thrown because of a problem or unfinished implementation in the 
 * GimpleCompiler
 */
public class InternalCompilerException extends RuntimeException {

  public InternalCompilerException() {
  }

  public InternalCompilerException(String message) {
    super(message);
  }

  public InternalCompilerException(String message, Throwable cause) {
    super(message, cause);
  }


  public InternalCompilerException(GimpleFunction function, Exception e) {
    super(String.format("Exception compiling function %s in unit %s", 
        function.getName(), 
        function.getUnit().getSourceFile().getName()), e);
  }

  public InternalCompilerException(FunctionGenerator functionGenerator, Exception e) {
    this(functionGenerator.getFunction(), e);
  }
}
