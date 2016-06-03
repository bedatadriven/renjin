package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Arrays;
import java.util.List;

/**
 * Strategy for passing fat pointer expressions as two arguments
 */
public class FatPtrParamStrategy implements ParamStrategy {
  
  private ValueFunction valueFunction;

  public FatPtrParamStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  @Override
  public List<Type> getParameterTypes() {
    Type arrayType = Type.getType("[" + valueFunction.getValueType().getDescriptor());
    Type offsetType = Type.INT_TYPE;
    
    return Arrays.asList(arrayType, offsetType);
  }

  @Override
  public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    return new FatPtrExpr(paramVars.get(0), paramVars.get(1));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<Expr> argument) {
    if(argument.isPresent()) {
      FatPtrExpr expr = (FatPtrExpr) argument.get();
      expr.getArray().load(mv);
      expr.getOffset().load(mv);
    } else {
      mv.aconst(null);
      mv.iconst(0);
    }
  }
}
