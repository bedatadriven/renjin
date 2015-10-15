package org.renjin.gcc.codegen.var;

import org.objectweb.asm.ClassVisitor;

/**
 * Generates field definitions, loads and stores for global variables
 */
public interface FieldGenerator extends VarGenerator {
  
  void emitField(ClassVisitor cv);
  
}
