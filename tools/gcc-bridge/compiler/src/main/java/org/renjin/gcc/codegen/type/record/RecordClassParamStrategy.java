package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;


public class RecordClassParamStrategy implements ParamStrategy {
  
  private RecordClassTypeStrategy strategy;

  public RecordClassParamStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }
  
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(strategy.getJvmType());
  }

  @Override
  public RecordValue emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(strategy.isUnitPointer()) {
      // If this type can be represented as a unit pointer, then 
      // the address expression is equivalent to the value expression.
      JLValue ref = paramVars.get(0);
      RecordUnitPtr address = new RecordUnitPtr(ref);
      
      return new RecordValue(ref, address);
   
    } else {
      if (parameter.isAddressable()) {
        JLValue array = localVars.reserveUnitArray(parameter.getName(), strategy.getJvmType(),
            Optional.of(Expressions.newObject(strategy.getJvmType())));

        FatPtrPair address = new FatPtrPair(new RecordClassValueFunction(strategy), array);
        RecordValue value = new RecordValue(Expressions.elementAt(array, 0), address);
        
        return value;
        
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
