package org.renjin.gcc.codegen.var;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * Allocates global variables that are declared in an existing class.
 */
public class ProvidedVarAllocator extends VarAllocator {
  
  private final Class declaringClass;

  public ProvidedVarAllocator(Class declaringClass) {
    this.declaringClass = declaringClass;
  }

  

  @Override
  public JLValue reserve(final String name, final Type type) {
    // verify that the field already exists in this class
    Field field;
    try {
      field = declaringClass.getField(name);
    } catch (NoSuchFieldException e) {
      throw new InternalCompilerException("The field '" + name + "' does not exist in class " + 
          declaringClass.getName());
    }
    Type declaredType = Type.getType(field.getType());
    if(!declaredType.equals(type)) {
      throw new InternalCompilerException(String.format(
          "Type mismatch between provided field '%s: expected %s but found %s", name, type, declaredType));
    }
    return new JLValue() {

      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(declaringClass), name, type.getDescriptor());
      }

      @Override
      public void store(MethodGenerator mv, JExpr value) {
        value.load(mv);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, Type.getInternalName(declaringClass), name, type.getDescriptor());
      }
    };
  }

  @Override
  public JLValue reserve(String name, Type type, JExpr initialValue) {
    throw new UnsupportedOperationException("TO CHECK");
  }
  
}
