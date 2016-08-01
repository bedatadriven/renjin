package org.renjin.gcc.codegen.type;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import java.lang.reflect.Constructor;


/**
 * Strategy for fields that can be represented by a {@link JExpr}
 */
public class SimpleFieldStrategy extends FieldStrategy {
  
  private Type fieldType;
  private String name;
  private Class<? extends GExpr> exprClass;
  

  /**
   * @param name the name of the field
   * @param fieldType the type of the field
   * @param exprClass
   */
  public SimpleFieldStrategy(String name, Type fieldType, Class<? extends GExpr> exprClass) {
    this.fieldType = fieldType;
    this.name = name;
    this.exprClass = exprClass;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, fieldType.getDescriptor(), null, null);
  }

  @Override
  public GExpr memberExpr(JExpr instance, int fieldOffset, GimpleType expectedType) {
    FieldValue fieldValue = new FieldValue(instance, name, fieldType);
    Constructor<? extends GExpr> constructor = null;
    try {
      constructor = exprClass.getConstructor(JExpr.class);
    } catch (NoSuchMethodException e) {
      throw new InternalCompilerException("GExpr class " + exprClass.getName() + " must have a constructor accepting " +
          "a single JExpr argument.");
    }
    GExpr member;
    try {
      member = constructor.newInstance(fieldValue);
    } catch (Exception e) {
      throw new InternalCompilerException("Error instantiating " + exprClass.getName(), e);
    }
    return member;
  }

}
