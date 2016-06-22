package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class RecordConstructor implements JExpr {
  
  private RecordClassTypeStrategy strategy;
  private Map<GimpleFieldRef, GExpr> fields;

  public RecordConstructor(RecordClassTypeStrategy strategy, Map<GimpleFieldRef, GExpr> fields) {
    this.strategy = strategy;
    this.fields = fields;
  }

  public RecordConstructor(RecordClassTypeStrategy strategy) {
    this(strategy, Collections.<GimpleFieldRef, GExpr>emptyMap());
  }

  @Nonnull
  @Override
  public Type getType() {
    return strategy.getJvmType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    mv.visitTypeInsn(Opcodes.NEW, strategy.getJvmType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, strategy.getJvmType().getInternalName(), "<init>", "()V", false);

    JExpr instance = new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return strategy.getJvmType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.dup();
      }
    };
    
    for (Map.Entry<GimpleFieldRef, GExpr> field : fields.entrySet()) {
      // Push the value onto the stack and save to the field
      GExpr fieldExpr = strategy.memberOf(new RecordValue(instance), field.getKey());
      try {
        fieldExpr.store(mv, field.getValue());
      } catch (Exception e) {
        throw new InternalCompilerException(
            String.format("Exception storing value for field %s of type %s in %s", 
                field.getKey(),
                field.getKey().getType(),
                strategy.getClass().getSimpleName()), e);
      }
    }
  }
}
