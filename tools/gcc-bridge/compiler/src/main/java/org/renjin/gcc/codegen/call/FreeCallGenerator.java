package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates a call to free(ptr). 
 */
public class FreeCallGenerator implements CallGenerator {
  @Override
  public void emitCall(MethodGenerator mv, List<ExprGenerator> argumentGenerators) {
    if(argumentGenerators.size() != 1) {
      throw new InternalCompilerException("Expected single argument to free, found " +
        argumentGenerators.size() + " arguments");
    }

    // this is a no op - but not sure we can be sure that 
    // there are no side effects 
    ExprGenerator ptr = argumentGenerators.get(0);
    ptr.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.POP2);
  }

  @Override
  public void emitCallAndPopResult(MethodGenerator visitor, List<ExprGenerator> argumentGenerators) {
    emitCall(visitor, argumentGenerators);
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    throw new UnsupportedOperationException();
  }
}
