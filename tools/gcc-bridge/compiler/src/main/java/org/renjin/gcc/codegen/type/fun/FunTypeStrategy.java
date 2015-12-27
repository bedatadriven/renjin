package org.renjin.gcc.codegen.type.fun;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimplePointerType;

import java.lang.invoke.MethodHandle;
import java.util.List;

/**
 * Creates {@code Generators} for values for function values.
 * 
 * <p>Function pointers are compiled to {@link java.lang.invoke.MethodHandle}s, but since Gimple
 * is statically typed, we don't need the {@code invokedynamic} bytecode and can simply use
 * {@link java.lang.invoke.MethodHandle#invokeExact(Object...)} to invoke function calls.</p>
 */ 
public class FunTypeStrategy extends TypeStrategy {

  private GimpleFunctionType type;

  public FunTypeStrategy(GimpleFunctionType type) {
    this.type = type;
  }

  @Override
  public TypeStrategy pointerTo() {
    return new Pointer();
  }

  private class Pointer extends TypeStrategy {
    @Override
    public ParamStrategy getParamStrategy() {
      return new FunPtrParamStrategy(new GimplePointerType(type));
    }

    @Override
    public VarGenerator varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return new FunPtrVarGenerator(type, allocator.reserve(decl.getName(), Type.getType(MethodHandle.class)));
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new FunPtrFieldGenerator(className, fieldName, type);
    }

    @Override
    public ReturnStrategy getReturnStrategy() {
      return new FunPtrReturnStrategy();
    }

    @Override
    public TypeStrategy pointerTo() {
      return new PointerPointer();
    }

    @Override
    public TypeStrategy arrayOf(GimpleArrayType arrayType) {
      return new PointerArray(arrayType);
    }
    
  }
  
  private class PointerPointer extends TypeStrategy {

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new FunPtrPtrField(className, fieldName, type); 
    }
  }
  
  private class PointerArray extends TypeStrategy {
    private GimpleArrayType arrayType;

    public PointerArray(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
      return new FunPtrArrayField(className, fieldName, arrayType);
    }

    @Override
    public ExprGenerator constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
      List<ExprGenerator> elements = Lists.newArrayList();
      for (GimpleConstructor.Element element : value.getElements()) {
        elements.add(exprFactory.findGenerator(element.getValue()));
      }
      return new FunPtrArrayConstructor(arrayType, elements);
    }
  }
  
}
