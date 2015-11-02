package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.renjin.gcc.codegen.var.VarGenerator;

/**
 * Generates field definitions, loads and stores for global variables
 */
public interface FieldGenerator extends VarGenerator {
  
  void emitField(ClassVisitor cv);
  
}
