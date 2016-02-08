package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.PtrExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.var.Value;

/**
 * Creates generators for void value types. Only used for return types.
 */
public class VoidTypeStrategy extends TypeStrategy {

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidReturnStrategy();
  }


  @Override
  public PtrExpr malloc(MethodGenerator mv, Value length) {
    throw new InternalCompilerException("Cannot allocate VOID type");
  }

}
