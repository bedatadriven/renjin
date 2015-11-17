package org.renjin.gcc.codegen.type;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.VoidPtrParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.ret.VoidReturnGenerator;

/**
 * Creates generators for void value types. Only used for return types.
 */
public class VoidTypeFactory extends TypeFactory {

  @Override
  public ReturnGenerator returnGenerator() {
    return new VoidReturnGenerator();
  }

  @Override
  public TypeFactory pointerTo() {
    return new Pointer();
  }

  @Override
  public ExprGenerator mallocExpression(ExprGenerator size) {
    throw new InternalCompilerException("Cannot allocate VOID type");
  }

  private class Pointer extends TypeFactory {
    @Override
    public ParamGenerator paramGenerator() {
      return new VoidPtrParamGenerator();
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      throw new InternalCompilerException("Cannot allocate void* type");
    }
  }
}
