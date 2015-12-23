package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.VarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.field.VoidPtrField;
import org.renjin.gcc.codegen.param.ParamStrategy;
import org.renjin.gcc.codegen.param.VoidPtrParamStrategy;
import org.renjin.gcc.codegen.ret.ReturnStrategy;
import org.renjin.gcc.codegen.ret.VoidPtrReturnStrategy;
import org.renjin.gcc.codegen.ret.VoidReturnStrategy;
import org.renjin.gcc.codegen.var.AddressableVoidPtrVar;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.GimpleVarDecl;

/**
 * Creates generators for void value types. Only used for return types.
 */
public class VoidTypeStrategy extends TypeStrategy {

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidReturnStrategy();
  }

  @Override
  public TypeStrategy pointerTo() {
    return new Pointer();
  }

  @Override
  public ExprGenerator mallocExpression(ExprGenerator size) {
    throw new InternalCompilerException("Cannot allocate VOID type");
  }

  private class Pointer extends TypeStrategy {
    @Override
    public ParamStrategy getParamStrategy() {
      return new VoidPtrParamStrategy();
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new VoidPtrField(className, fieldName);
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new AddressableVoidPtrVar(
          allocator.reserveArrayRef(decl.getName(), Type.getType(Object.class)));
    }

    @Override
    public ExprGenerator mallocExpression(ExprGenerator size) {
      throw new InternalCompilerException("Cannot allocate void* type");
    }

    @Override
    public ReturnStrategy getReturnStrategy() {
      return new VoidPtrReturnStrategy();
    }

    @Override
    public TypeStrategy pointerTo() {
      return new PointerPointer();
    }
  }

  private class PointerPointer extends TypeStrategy {

    @Override
    public ParamStrategy getParamStrategy() {
      return super.getParamStrategy();
    }
  }
}
