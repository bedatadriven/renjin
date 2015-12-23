package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;
import org.renjin.gcc.runtime.Ptr;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for {@code void*} parameters, implemented using a {@link Ptr} parameter.
 */
public class VoidPtrParamStrategy implements ParamStrategy {
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(Type.getType(Ptr.class));
  }

  @Override
  public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, VarAllocator localVars) {
    return new Expr(paramVars.get(0));
  }

  @Override
  public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
    parameterValueGenerator.emitPushRecordRef(mv);
  }

  private class Expr extends AbstractExprGenerator {
    private Var var;

    public Expr(Var var) {
      this.var = var;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(new GimpleVoidType());
    }


    @Override
    public void emitPushRecordRef(MethodVisitor mv) {
      var.load(mv);
    }
  }
}
