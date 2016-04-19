package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.LValue;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class RecordConstructor implements SimpleExpr {
  
  private RecordClassTypeStrategy strategy;
  private Map<GimpleFieldRef, Expr> fields;

  public RecordConstructor(RecordClassTypeStrategy strategy, Map<GimpleFieldRef, Expr> fields) {
    this.strategy = strategy;
    this.fields = fields;
  }

  public RecordConstructor(RecordClassTypeStrategy strategy) {
    this(strategy, Collections.<GimpleFieldRef, Expr>emptyMap());
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

    SimpleExpr instance = new SimpleExpr() {
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
    
    for (Map.Entry<GimpleFieldRef, Expr> field : fields.entrySet()) {
      // Push the value onto the stack and save to the field
      LValue fieldGenerator = (LValue) strategy.memberOf(instance, field.getKey());
      fieldGenerator.store(mv, field.getValue());
    }
  }
}
