package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Allocates global variables as static fields within a class.
 */
public class GlobalVarAllocator extends VarAllocator {

  private class StaticField implements SimpleLValue {

    private String name;
    private Type type;
    private Optional<SimpleExpr> initialValue;

    public StaticField(String name, Type type, Optional<SimpleExpr> initialValue) {
      this.name = name;
      this.type = type;
      this.initialValue = initialValue;
    }

    @Nonnull
    @Override
    public Type getType() {
      return type;
    }

    @Override
    public void load(@Nonnull MethodGenerator mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void store(MethodGenerator mv, SimpleExpr value) {
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
  public SimpleLValue reserve(String name, Type type) {
    return reserve(name, type, Optional.<SimpleExpr>absent());
  }

  public SimpleLValue reserve(String name, Type type, Optional<SimpleExpr> initialValue) {
    if(name.contains(".")) {
      throw new InternalCompilerException("illegal global variable name: " + name);
    }
    StaticField field = new StaticField(name, type, initialValue);
    fields.add(field);
    return field;
  }

  @Override
  public SimpleLValue reserve(String name, Type type, SimpleExpr initialValue) {
    return reserve(name, type, Optional.of(initialValue));
  }

  public void writeFields(ClassVisitor cv) {
    for (StaticField field : fields) {
      cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, field.name, field.type.getDescriptor(), null, null);
    }
  }
}
