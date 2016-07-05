package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldStrategy {

  public void emitInstanceInit(MethodGenerator mv) {
  }

  public abstract void writeFields(ClassVisitor cv);

  public abstract GExpr memberExpr(JExpr instance, GimpleType expectedType);


}
