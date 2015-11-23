package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.field.FunPtrFieldGenerator;
import org.renjin.gcc.codegen.param.FunPtrParamGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.var.FunPtrVarGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimplePointerType;

/**
 * Creates {@code Generators} for values for function values.
 * 
 * <p>Function pointers are compiled to {@link java.lang.invoke.MethodHandle}s, but since Gimple
 * is statically typed, we don't need the {@code invokedynamic} bytecode and can simply use
 * {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} to invoke function calls.</p>
 */ 
public class FunTypeFactory extends TypeFactory {

  private GimpleFunctionType type;

  public FunTypeFactory(GimpleFunctionType type) {
    this.type = type;
  }

  @Override
  public TypeFactory pointerTo() {
    return new Pointer();
  }

  private class Pointer extends TypeFactory {
    @Override
    public ParamGenerator paramGenerator() {
      return new FunPtrParamGenerator(new GimplePointerType(type));
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
      return new FunPtrVarGenerator(type, allocator.reserveObject());
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new FunPtrFieldGenerator(className, fieldName, type);
    }
  }
}
