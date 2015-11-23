package org.renjin.gcc.codegen.field;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.UnimplementedException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates field definitions, loads and stores for global variables
 */
public abstract class FieldGenerator {
  
  public abstract GimpleType getType();

  public abstract void emitStaticField(ClassVisitor cv, GimpleVarDecl decl);

  public void emitStaticInit(MethodVisitor mv) {
    
  }
  
  public void emitInstanceInit(MethodVisitor mv) {
    
  }

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

  /**
   * Emits the bytecode to store a value to the record currently on the stack.
   */
  public void emitStoreMember(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnimplementedException(getClass(), "emitStoreMember");
  }


  protected final void assertNoInitialValue(GimpleVarDecl decl) {
    if(decl.getValue() != null) {
      throw new UnsupportedOperationException("Initial values not implemented for " + getClass().getName());
    }
  }

}
