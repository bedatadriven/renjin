package org.renjin.gcc.codegen.type;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldStrategy {

  public void emitInstanceInit(MethodGenerator mv) {
  }

  public abstract void writeFields(ClassVisitor cv);

  public abstract Expr memberExprGenerator(SimpleExpr instanceGenerator);

}
