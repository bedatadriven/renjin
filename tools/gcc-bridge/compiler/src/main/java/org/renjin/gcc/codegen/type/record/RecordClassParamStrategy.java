package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
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
  public RecordValue emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(unitPointer) {
      // If this type can be represented as a unit pointer, then 
      // the address expression is equivalent to the value expression.
      JLValue ref = paramVars.get(0);
      RecordUnitPtr address = new RecordUnitPtr(ref);
      
      return new RecordValue(ref, address);
   
    } else {
      if (parameter.isAddressable()) {
        throw new UnsupportedOperationException("TODO");
      } else {
        return new RecordValue(paramVars.get(0));
      }
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      RecordValue recordValue = (RecordValue) argument.get();
      recordValue.getRef().load(mv);
      // We are passing by value, so we need to put a clone of the record on the stack
      mv.invokevirtual(recordValue.getJvmType(), "clone", Type.getMethodDescriptor(recordValue.getJvmType()), false);
      
    } else {
      mv.aconst(null);
    }
  }
}
