package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.ClassVisitor;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldStrategy {

  public void emitInstanceInit(MethodGenerator mv) {
  }

  public abstract void writeFields(ClassVisitor cv);


  /**
   * Returns a {@code {@link GExpr} for this field of the given {@code instance}.
   * 
   * @param instance the instance of the class from which to access the member
   * @param offset the offset, in bits, from the <em>start</em> of this field.
   * @param size The size of the value, in bits, to access.
   * @param expectedType The strategy for the expected type of the field.
   */
  public abstract GExpr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType);
  
  public abstract void copy(MethodGenerator mv, JExpr source, JExpr dest);
  
  public abstract void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount);

}
