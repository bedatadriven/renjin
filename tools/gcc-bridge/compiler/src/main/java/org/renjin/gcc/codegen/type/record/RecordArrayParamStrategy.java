package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;


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
  public RecordArrayExpr emitInitialization(MethodGenerator methodVisitor, 
                                 GimpleParameter parameter, 
                                 List<JLValue> paramVars, 
                                 VarAllocator localVars) {


    return new RecordArrayExpr(paramVars.get(0), arrayLength);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {

    // We're passing by VALUE, so we have to make a copy of the array.
    if(argument.isPresent()) {
      RecordArrayExpr recordVar = (RecordArrayExpr) argument.get();
      JExpr arrayCopy = recordVar.copyArray();

      arrayCopy.load(mv);
    } else {
      // Argument not supplied, stack will be corrupted
      mv.aconst(null);
    }
  }
}
