package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleAddressableExpr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;


public class RecordClassParamStrategy implements ParamStrategy {
  
  private Type jvmType;
  private boolean unitPointer;

  public RecordClassParamStrategy(Type jvmType, boolean unitPointer) {
    this.jvmType = jvmType;
    this.unitPointer = unitPointer;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(jvmType);
  }

  @Override
  public Expr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<SimpleLValue> paramVars, VarAllocator localVars) {
    if(unitPointer) {
      // If this type can be represented as a unit pointer, then 
      // the address expression is equivalent to the value expression.
      SimpleLValue ref = paramVars.get(0);
      SimpleExpr address = ref;
      
      return new SimpleAddressableExpr(ref, address);
   
    } else {
      if (parameter.isAddressable()) {
        throw new UnsupportedOperationException("TODO");
      } else {
        return paramVars.get(0);
      }
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<Expr> argument) {
    if(argument.isPresent()) {
      SimpleExpr ref = (SimpleExpr) argument.get();
      ref.load(mv);
      // We are passing by value, so we need to put a clone of the record on the stack
      mv.invokevirtual(ref.getType(), "clone", Type.getMethodDescriptor(ref.getType()), false);
      
    } else {
      mv.aconst(null);
    }
  }
}
