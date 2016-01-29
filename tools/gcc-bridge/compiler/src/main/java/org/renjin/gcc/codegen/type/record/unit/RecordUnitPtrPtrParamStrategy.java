package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for a parameter that is a pointer to one or more unit record pointers, implemented using a single
 * {@link ObjectPtr} parameter. Each element of {@link ObjectPtr#array} is a reference to the record's JVM class.
 */
public class RecordUnitPtrPtrParamStrategy implements ParamStrategy {
  
  private RecordClassTypeStrategy strategy;

  public RecordUnitPtrPtrParamStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return new ParamExpr(paramVars.get(0));
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }

  private class ParamExpr extends AbstractExprGenerator {

    private Var var;

    public ParamExpr(Var var) {
      this.var = var;
    }

    @Override
    public GimpleType getGimpleType() {
      return strategy.getRecordType().pointerTo().pointerTo();
    }

    @Override
    public ExprGenerator valueOf() {
      return new DereferencedUnitRecordPtr(strategy, this);
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      var.load(mv);
      Type arrayType = Type.getType("[" + strategy.getJvmType().getDescriptor());
      WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv, Optional.of(arrayType));
    }

    @Override
    public void emitPushPtrRefForNullComparison(MethodGenerator mv) {
      var.load(mv);
      mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(ObjectPtr.class), "array", "[Ljava/lang/Object;");
    }
  }
}
