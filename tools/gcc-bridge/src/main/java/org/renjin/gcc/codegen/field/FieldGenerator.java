package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldGenerator {
  
  public abstract void emitStaticField(ClassVisitor cv, GimpleVarDecl decl);
  
  public abstract void emitInstanceField(ClassVisitor cv);

  /**
   * 
   * @return an {@code ExprGenerator} that generates code for reading and writing to a static field
   */
  public abstract ExprGenerator staticExprGenerator();

  /**
   * 
   * @param instanceGenerator an {@code ExprGenerator} that can read the record's instance 
   * @return an {@code ExprGenerator} that can generate loads/stores for this field.
   */
  public abstract ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator);
  
  
  protected final void assertNoInitialValue(GimpleVarDecl decl) {
    if(decl.getValue() != null) {
      throw new UnsupportedOperationException("Initial values not implemented for " + getClass().getName());
    }
  }
  
}
