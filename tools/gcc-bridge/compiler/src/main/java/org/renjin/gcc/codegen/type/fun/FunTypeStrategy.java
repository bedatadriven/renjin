package org.renjin.gcc.codegen.type.fun;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleFunctionType;

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

  private static final Type METHOD_HANDLE_TYPE = Type.getType(MethodHandle.class);
  
  private GimpleFunctionType type;

  public FunTypeStrategy(GimpleFunctionType type) {
    this.type = type;
  }

  @Override
  public TypeStrategy pointerTo() {
    return new Pointer();
  }

  /**
   * Strategy for Function Pointers
   */
  private class Pointer extends TypeStrategy {
    @Override
    public ParamStrategy getParamStrategy() {
      return new ValueParamStrategy(METHOD_HANDLE_TYPE);
    }

    @Override
    public Var varGenerator(GimpleVarDecl decl, VarAllocator allocator) {
      return allocator.reserve(decl.getName(), Type.getType(MethodHandle.class));
    }

    @Override
    public FieldStrategy fieldGenerator(String className, String fieldName) {
      return new ValueFieldStrategy(METHOD_HANDLE_TYPE, fieldName);
    }

    @Override
    public ReturnStrategy getReturnStrategy() {
      return new ValueReturnStrategy(METHOD_HANDLE_TYPE);
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
    public FieldStrategy fieldGenerator(String className, String fieldName) {
      return new ValueFieldStrategy(METHOD_HANDLE_TYPE, fieldName); 
    }
  }
  
  private class PointerArray extends TypeStrategy {
    private GimpleArrayType arrayType;

    public PointerArray(GimpleArrayType arrayType) {
      this.arrayType = arrayType;
    }

    @Override
    public FieldStrategy fieldGenerator(String className, String fieldName) {
     // TODO: return new FunPtrArrayField(className, fieldName, arrayType);
      throw new UnsupportedOperationException();
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
