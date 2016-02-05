package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;

import java.util.List;

/**
 * Allocates global variables as static fields within a class.
 */
public class GlobalVarAllocator extends VarAllocator {

  private class StaticField implements Var {

    private String name;
    private Type type;
    private Optional<Value> initialValue;

    public StaticField(String name, Type type, Optional<Value> initialValue) {
      this.name = name;
      this.type = type;
      this.initialValue = initialValue;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public void load(MethodGenerator mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void store(MethodGenerator mv, Value value) {
      value.load(mv);
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
    return reserve(name, type, Optional.<Value>absent());
  }

  public Var reserve(String name, Type type, Optional<Value> initialValue) {
    if(name.contains(".")) {
      throw new InternalCompilerException("illegal global variable name: " + name);
    }
    StaticField field = new StaticField(name, type, initialValue);
    fields.add(field);
    return field;
  }

  @Override
  public Var reserve(String name, Type type, Value initialValue) {
    return reserve(name, type, Optional.of(initialValue));
  }

  public void writeFields(ClassVisitor cv) {
    for (StaticField field : fields) {
      cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, field.name, field.type.getDescriptor(), null, null);
    }
  }
}
