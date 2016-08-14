package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class RecordConstructor implements JExpr {
  
  private final TypeOracle typeOracle;
  private RecordClassTypeStrategy strategy;
  private Map<GimpleFieldRef, GExpr> fields;

  public RecordConstructor(TypeOracle typeOracle, RecordClassTypeStrategy strategy, Map<GimpleFieldRef, GExpr> fields) {
    this.typeOracle = typeOracle;
    this.strategy = strategy;
    this.fields = fields;
  }

  public RecordConstructor(RecordClassTypeStrategy strategy) {
    this(null, strategy, Collections.<GimpleFieldRef, GExpr>emptyMap());
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

    if (!fields.entrySet().isEmpty()) {

      LocalVarAllocator.LocalVar instanceVar = mv.getLocalVarAllocator().reserve(strategy.getJvmType());
      instanceVar.store(mv);

      for (Map.Entry<GimpleFieldRef, GExpr> field : fields.entrySet()) {
        // Push the value onto the stack and save to the field
        GimpleFieldRef fieldRef = field.getKey();
        TypeStrategy fieldTypeStrategy = typeOracle.forType(fieldRef.getType());
        GExpr fieldExpr = strategy.memberOf(mv, 
            new RecordValue(instanceVar),
            fieldRef.getOffset(),
            fieldRef.getSize(),
            fieldTypeStrategy);

        try {
          fieldExpr.store(mv, field.getValue());
        } catch (Exception e) {
          throw new InternalCompilerException(
              String.format("Exception storing value for field %s of type %s in %s",
                  fieldRef,
                  fieldRef.getType(),
                  strategy.getClass().getSimpleName()), e);
        }
      }

      instanceVar.load(mv);
    }
  }
}
