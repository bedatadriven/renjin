package org.renjin.gcc.codegen.type;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldStrategy {

  public void emitInstanceInit(MethodGenerator mv) {
  }

  public abstract void emitInstanceField(ClassVisitor cv);

  /**
   *
   * @param instanceGenerator an {@code ExprGenerator} that can read the record's instance 
   * @return an {@code ExprGenerator} that can generate loads/stores for this field.
   */
  public abstract ExprGenerator memberExprGenerator(Value instanceGenerator);

  /**
   * Emits the bytecode to store a value to the record currently on the stack.
   */
  public void emitStoreMember(MethodGenerator mv, ExprGenerator valueGenerator) {
    throw new UnimplementedException(getClass(), "emitStoreMember");
  }

}
