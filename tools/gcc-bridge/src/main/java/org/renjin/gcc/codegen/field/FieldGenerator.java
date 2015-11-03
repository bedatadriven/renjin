package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;

/**
 * Generates field definitions, loads and stores for global variables
 */
public interface FieldGenerator {
  
  void emitStaticField(ClassVisitor cv);
  
  void emitInstanceField(ClassVisitor cv);

  /**
   * 
   * @return an {@code ExprGenerator} that generates code for reading and writing to a static field
   */
  ExprGenerator staticExprGenerator();

  /**
   * 
   * @param instanceGenerator an {@code ExprGenerator} that can read the record's instance 
   * @return an {@code ExprGenerator} that can generate loads/stores for this field.
   */
  ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator);
}
