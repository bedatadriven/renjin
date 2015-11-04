package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

public interface ReturnGenerator {

  /**
   * 
   * @return the JVM return type
   */
  Type getType();

  /**
   * 
   * @return the Gimple return type
   */
  GimpleType getGimpleType();

  /**
   * Generate the bytecode to return the given {@code valueGenerator} from the method.
   */
  void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator);
  
  void emitVoidReturn(MethodVisitor mv);
  
  ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments);

}
