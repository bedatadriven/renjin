package org.renjin.gcc.codegen.type.record.unit;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

/**
 * Created by alex on 16-6-16.
 */
public class RecordUnitPtrParamStrategy implements ParamStrategy {
  
  private Type jvmType;

  public RecordUnitPtrParamStrategy(Type jvmType) {
    this.jvmType = jvmType;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(jvmType);
  }

  @Override
  public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return paramVars.get(0);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<Expr> argument) {
    if(argument.isPresent()) {
      SimpleExpr value = (SimpleExpr) argument.get();

      if (value.getType().equals(jvmType)) {
        value.load(mv);

      } else {
        // Cast null pointers to the appropriate type
        Expressions.cast(value, this.jvmType).load(mv);
      }  
    } else {
      mv.aconst(null);
    }
  }
}
