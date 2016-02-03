package org.renjin.gcc.codegen.type.fun;

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
  
  
}
