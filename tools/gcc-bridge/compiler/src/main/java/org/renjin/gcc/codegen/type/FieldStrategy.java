package org.renjin.gcc.codegen.type;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldStrategy {

  public void emitInstanceInit(MethodGenerator mv) {
  }

  public abstract void emitInstanceField(ClassVisitor cv);

  public abstract ExprGenerator memberExprGenerator(Value instanceGenerator);

}
