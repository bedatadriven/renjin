package org.renjin.gcc.codegen.type.record.fat;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for a record pointer parameter that may point to more than one record value, implemented using
 * a {@link ObjectPtr} reference, where each element of the {@link ObjectPtr#array} is an instance of the 
 * JVM class backing the parameter's record type.
 */
public class RecordFatPtrParamStrategy implements ParamStrategy {
  
  private RecordFatPtrStrategy strategy;

  public RecordFatPtrParamStrategy(RecordFatPtrStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(ObjectPtr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return new Expr(Iterables.getOnlyElement(paramVars));
  }

  @Override
  public void emitPushParameter(MethodGenerator mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushPointerWrapper(mv);
  }
  
  private class Expr extends AbstractExprGenerator {
    private Var param;

    public Expr(Var param) {
      this.param = param;
    }

    @Override
    public GimpleType getGimpleType() {
      return strategy.getGimpleType();
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      param.load(mv);
      WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv, Optional.of(strategy.getJvmArrayType()));
    }

    @Override
    public void emitPushRecordRef(MethodGenerator mv) {
      emitPushPtrArrayAndOffset(mv);
      mv.visitInsn(Opcodes.AALOAD);
    }
  }

  
  
}
