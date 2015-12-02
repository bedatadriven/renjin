package org.renjin.gcc.codegen.field;

import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;


public class FunPtrArrayConstructor extends AbstractExprGenerator {
  
  private GimpleArrayType arrayType;
  private List<ExprGenerator> elements;

  public FunPtrArrayConstructor(GimpleArrayType arrayType, List<ExprGenerator> elements) {
    this.arrayType = arrayType;
    this.elements = elements;
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType;
  }
  
  
}
