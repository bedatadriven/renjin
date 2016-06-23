package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.fun.FunctionRefGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.runtime.MallocThunk;

/**
 * Generates a call to free(ptr). 
 */
public class FreeCallGenerator implements CallGenerator, MethodHandleGenerator {

  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
    //  NOOP we have a garbage collector
  }

  @Override
  public JExpr getMethodHandle() {
    return new FunctionRefGenerator(new Handle(Opcodes.H_INVOKESTATIC, 
        Type.getInternalName(MallocThunk.class), "free", 
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class))));
  }
}
