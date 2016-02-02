package org.renjin.gcc.codegen.type;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.gcc.codegen.var.Value;


public class ValueFieldStrategy extends FieldStrategy {
  
  private Type type;
  private String name;

  public ValueFieldStrategy(Type type, String name) {
    this.type = type;
    this.name = name;
  }

  @Override
  public void emitInstanceField(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, type.getDescriptor(), null, null);
  }

  @Override
  public ExprGenerator memberExprGenerator(ExprGenerator instanceGenerator) {
    return new FieldValue((Value) instanceGenerator, name, type);
  }
}
