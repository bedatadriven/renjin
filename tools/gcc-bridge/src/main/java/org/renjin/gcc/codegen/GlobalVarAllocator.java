package org.renjin.gcc.codegen;

import com.google.common.collect.Lists;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;

import java.util.List;

/**
 * Allocates global variables as static fields within a class.
 */
public class GlobalVarAllocator extends VarAllocator {

  private class StaticField implements Var {

    private String name;
    private Type type;

    public StaticField(String name, Type type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public void load(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void store(MethodVisitor mv) {
      mv.visitFieldInsn(Opcodes.PUTSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }
  }
  
  private final Type declaringClass;
  private final List<StaticField> fields = Lists.newArrayList();

  public GlobalVarAllocator(String declaringClass) {
    this.declaringClass = Type.getType(declaringClass);
  }
  
  @Override
  public Var reserve(String name, Type type) {
    if(name.contains(".")) {
      throw new InternalCompilerException("illegal global variable name: " + name);
    }
    StaticField field = new StaticField(name, type);
    fields.add(field);
    return field;
  }
  
  public void writeFields(ClassVisitor cv) {
    for (StaticField field : fields) {
      cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, field.name, field.type.getDescriptor(), null, null);
    }
  }
}
