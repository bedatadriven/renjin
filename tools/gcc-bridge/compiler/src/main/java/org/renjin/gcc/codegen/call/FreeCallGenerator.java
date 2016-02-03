package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates a call to free(ptr). 
 */
public class FreeCallGenerator implements CallGenerator {

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    //  NOOP we have a garbage collector
    
  }

}
