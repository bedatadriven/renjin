package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Allocates global variables as static fields within a class.
 */
public class GlobalVarAllocator extends VarAllocator {

  private class StaticField implements JLValue {

    private String name;
    private Type type;
    private Optional<JExpr> initialValue;

    public StaticField(String name, Type type, Optional<JExpr> initialValue) {
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
    public void store(MethodGenerator mv, JExpr value) {
      value.load(mv);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }
  }
  
  private final Type declaringClass;
  private final List<StaticField> fields = Lists.newArrayList();
  private final Set<String> fieldNames = Sets.newHashSet();

  public GlobalVarAllocator(String declaringClass) {
    this.declaringClass = Type.getType(declaringClass);
  }

  @Override
  public JLValue reserve(String name, Type type) {
    return reserve(name, type, Optional.<JExpr>absent());
  }

  public JLValue reserve(String name, Type type, Optional<JExpr> initialValue) {
    String fieldName = toJavaSafeName(name);
    if(fieldNames.contains(fieldName)) {
      throw new InternalCompilerException("Duplicate field name generated '" + name + "' [" + fieldName + "]");
    }
    fieldNames.add(fieldName);
    StaticField field = new StaticField(fieldName, type, initialValue);
    fields.add(field);
    return field;
  }

  @Override
  public JLValue reserve(String name, Type type, JExpr initialValue) {
    return reserve(name, type, Optional.of(initialValue));
  }

  public void writeFields(ClassVisitor cv) {
    for (StaticField field : fields) {
      cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, field.name, field.type.getDescriptor(), null, null);
    }
  }
}
