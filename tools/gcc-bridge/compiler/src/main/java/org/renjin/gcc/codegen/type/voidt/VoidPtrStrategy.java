package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.runtime.ObjectPtr;


public class VoidPtrStrategy implements PointerTypeStrategy<SimpleExpr> {
  @Override
  public SimpleExpr malloc(MethodGenerator mv, SimpleExpr length) {
    throw new InternalCompilerException("(void*)malloc() not supported.");
  }

  @Override
  public SimpleExpr realloc(SimpleExpr pointer, SimpleExpr length) {
    throw new InternalCompilerException("(void*)malloc() not supported.");
  }

  @Override
  public SimpleExpr pointerPlus(SimpleExpr pointer, SimpleExpr offsetInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Expr valueOf(SimpleExpr pointerExpr) {
    throw new UnsupportedOperationException("void pointers cannot be dereferenced.");
  }

  @Override
  public SimpleExpr nullPointer() {
    return Expressions.nullRef(Type.getType(Object.class));
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, SimpleExpr x, SimpleExpr y) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr memoryCompare(SimpleExpr p1, SimpleExpr p2, SimpleExpr n) {
    return new VoidPtrMemCmp(p1, p2, n);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, SimpleExpr destination, SimpleExpr source, SimpleExpr length) {
    
    destination.load(mv);
    source.load(mv);
    length.load(mv);
    
    mv.invokestatic(ObjectPtr.class, "memcpy", Type.getMethodDescriptor(Type.VOID_TYPE, 
          Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE));
  }

  @Override
  public void memorySet(MethodGenerator mv, SimpleExpr pointer, SimpleExpr byteValue, SimpleExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new VoidPtrParamStrategy();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(Type.getType(Object.class));
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      throw new UnsupportedOperationException("TODO");
    }
    return allocator.reserve(decl.getName(), Type.getType(Object.class));
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new VoidPtrField(fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    throw new UnsupportedOperationException("TODO");
  }
}
