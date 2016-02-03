package org.renjin.gcc.codegen.var;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;

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
  public Var reserve(final String name, final Type type) {
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
    return new Var() {

      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(MethodGenerator mv) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(declaringClass), name, type.getDescriptor());
      }

      @Override
      public void store(MethodGenerator mv, Value value) {
        value.load(mv);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, Type.getInternalName(declaringClass), name, type.getDescriptor());
      }
    };
  }
}
