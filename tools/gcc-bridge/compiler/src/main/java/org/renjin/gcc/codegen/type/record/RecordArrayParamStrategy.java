package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.expr.SimpleLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;

import java.util.Collections;
import java.util.List;

import static org.renjin.gcc.codegen.expr.Expressions.copyOfArrayRange;
import static org.renjin.gcc.codegen.expr.Expressions.sum;


public class RecordArrayParamStrategy implements ParamStrategy {
  
  private final Type arrayType;
  private final int arrayLength;

  public RecordArrayParamStrategy(Type arrayType, int arrayLength) {
    this.arrayType = arrayType;
    this.arrayLength = arrayLength;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(arrayType);
  }

  @Override
  public RecordArrayVar emitInitialization(MethodGenerator methodVisitor, 
                                 GimpleParameter parameter, 
                                 List<SimpleLValue> paramVars, 
                                 VarAllocator localVars) {


    return new RecordArrayVar(paramVars.get(0), arrayLength);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Expr argument) {

    // We're passing by VALUE, so we have to make a copy of the array.
    RecordArrayVar recordVar = (RecordArrayVar) argument;
    SimpleExpr arrayCopy = copyOfArrayRange(
        recordVar.getArray(), 
        recordVar.getOffset(), 
        sum(recordVar.getOffset(), arrayLength));
    
    
    arrayCopy.load(mv);
  }
}
