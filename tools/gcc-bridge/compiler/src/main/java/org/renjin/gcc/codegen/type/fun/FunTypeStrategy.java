package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.unit.RefConditionGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleFunctionType;

import java.lang.invoke.MethodHandle;

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
  private class Pointer extends TypeStrategy<Value> {
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
      return new FatPtrStrategy(new FunPtrValueFunction(32));
    }

    @Override
    public TypeStrategy arrayOf(GimpleArrayType arrayType) {
      return new ArrayTypeStrategy(arrayType, new FunPtrValueFunction(32));
    }

    @Override
    public Value nullPointer() {
      return Values.nullRef(METHOD_HANDLE_TYPE);
    }

    @Override
    public ConditionGenerator comparePointers(GimpleOp op, Value x, Value y) {
      return new RefConditionGenerator(op, x, y);
    }

    @Override
    public ExprGenerator valueOf(Value pointerExpr) {
      return pointerExpr;
    }
  }
  
}
