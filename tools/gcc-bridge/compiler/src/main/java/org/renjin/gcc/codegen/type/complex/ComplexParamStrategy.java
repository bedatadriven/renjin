package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ConstantValue;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.repackaged.asm.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ComplexParamStrategy implements ParamStrategy {
  private final GimpleComplexType type;

  public ComplexParamStrategy(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Arrays.asList(type.getJvmPartType(), type.getJvmPartType());
  }

  @Override
  public List<String> getParameterNames(String name) {
    return Arrays.asList(name, name + "$i");
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    return new ComplexExpr(paramVars.get(0), paramVars.get(1));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    ComplexExpr value = argument
        .map(a -> (ComplexExpr) a)
        .orElse(new ComplexExpr(new ConstantValue(type.getJvmPartType(), 0)));

    value.getRealJExpr().load(mv);
    value.getImaginaryJExpr().load(mv);
  }
}
