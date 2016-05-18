package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 * Pointer to an empty record type.
 * 
 * <p>Because the record is zero length, we can represent it simply as a {@code java.lang.Object} reference,
 * which allows us to cast it back and forth between references to other records.</p>
 */
public class EmptyRecordPtrTypeStrategy implements PointerTypeStrategy<SimpleExpr> {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);

  @Override
  public SimpleExpr malloc(MethodGenerator mv, SimpleExpr sizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr realloc(SimpleExpr pointer, SimpleExpr newSizeInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr pointerPlus(SimpleExpr pointer, SimpleExpr offsetInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Expr valueOf(SimpleExpr pointerExpr) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr nullPointer() {
    return Expressions.nullRef(OBJECT_TYPE);
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, SimpleExpr x, SimpleExpr y) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr memoryCompare(SimpleExpr p1, SimpleExpr p2, SimpleExpr n) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memoryCopy(MethodGenerator mv, SimpleExpr destination, SimpleExpr source, SimpleExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void memorySet(MethodGenerator mv, SimpleExpr pointer, SimpleExpr byteValue, SimpleExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public SimpleExpr toVoidPointer(SimpleExpr ptrExpr) {
    return ptrExpr;
  }

  @Override
  public SimpleExpr unmarshallVoidPtrReturnValue(MethodGenerator mv, SimpleExpr voidPointer) {
    return voidPointer;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new SimpleParamStrategy(OBJECT_TYPE);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new SimpleReturnStrategy(Type.getType(Object.class));
  }

  @Override
  public SimpleExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    return allocator.reserve(decl.getName(), OBJECT_TYPE);
  }

  @Override
  public SimpleExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new SimpleFieldStrategy(fieldName, OBJECT_TYPE);
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
    return new ArrayTypeStrategy(arrayType, new EmptyRecordPtrValueFunction());
  }

  @Override
  public SimpleExpr cast(Expr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof RecordUnitPtrStrategy) {
      return (SimpleExpr) value;
    }
    throw new UnsupportedCastException();
  }

}
