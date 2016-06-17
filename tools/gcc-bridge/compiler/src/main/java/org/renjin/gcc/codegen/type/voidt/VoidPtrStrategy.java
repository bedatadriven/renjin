package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;


/**
 * Strategy for handling pointers of unknown type.
 * 
 * <p>GCC Bridge compiles {@code void *} types as values of type {@code java.lang.Object}.
 * Void pointers may point a Fat Pointer object such as {@link org.renjin.gcc.runtime.DoublePtr}, 
 * to a {@link java.lang.invoke.MethodHandle}, or to record type for records that use the 
 * {@link org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy}.</p>
 */
public class VoidPtrStrategy implements PointerTypeStrategy<VoidPtr>, SimpleTypeStrategy<VoidPtr> {
  @Override
  public VoidPtr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    return new org.renjin.gcc.codegen.type.voidt.VoidPtr(new NewMallocThunkExpr(sizeInBytes));
  }

  @Override
  public VoidPtr realloc(final VoidPtr pointer, JExpr newSizeInBytes) {
    return new VoidPtr(new VoidPtrRealloc(pointer.unwrap(), newSizeInBytes));
  }

  @Override
  public VoidPtr pointerPlus(VoidPtr pointer, JExpr offsetInBytes) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr valueOf(VoidPtr pointerExpr) {
    throw new UnsupportedOperationException("void pointers cannot be dereferenced.");
  }

  @Override
  public VoidPtr nullPointer() {
    return new VoidPtr(Expressions.nullRef(Type.getType(Object.class)));
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, VoidPtr x, VoidPtr y) {
    return new VoidPtrComparison(op, x.unwrap(), y.unwrap());
  }

  @Override
  public JExpr memoryCompare(VoidPtr p1, VoidPtr p2, JExpr n) {
    return new VoidPtrMemCmp(p1.unwrap(), p2.unwrap(), n);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, VoidPtr destination, VoidPtr source, JExpr length, boolean buffer) {
    
    destination.unwrap().load(mv);
    source.unwrap().load(mv);
    length.load(mv);
    
    mv.invokestatic(org.renjin.gcc.runtime.VoidPtr.class, "memcpy", 
        Type.getMethodDescriptor(Type.VOID_TYPE, 
            Type.getType(Object.class), Type.getType(Object.class), Type.INT_TYPE));
  }

  @Override
  public void memorySet(MethodGenerator mv, VoidPtr pointer, JExpr byteValue, JExpr length) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VoidPtr toVoidPointer(VoidPtr ptrExpr) {
    return ptrExpr;
  }

  @Override
  public VoidPtr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {
    return new VoidPtr(voidPointer);
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new VoidPtrParamStrategy();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new VoidPtrReturnStrategy();
  }

  @Override
  public VoidPtr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      JLValue unitArray = allocator.reserveArrayRef(decl.getName(), Type.getType(Object.class));
      FatPtrExpr address = new FatPtrExpr(unitArray);
      JExpr value = Expressions.elementAt(unitArray, 0);
      
      return new VoidPtr(value, address);
    
    } else {
      
      return new VoidPtr(allocator.reserve(decl.getNameIfPresent(), Type.getType(Object.class)));
    }
  }

  @Override
  public VoidPtr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new VoidPtrField(fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, new VoidPtrValueFunction());
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new FatPtrStrategy(new VoidPtrValueFunction());
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new VoidPtrValueFunction());
  }

  @Override
  public VoidPtr cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof PointerTypeStrategy) {
      return ((PointerTypeStrategy) typeStrategy).toVoidPointer(value);
    }
    throw new UnsupportedCastException();
  }

  @Override
  public String toString() {
    return "VoidPtrStrategy";
  }

  @Override
  public Type getJvmType() {
    return Type.getType(Object.class);
  }

  @Override
  public VoidPtr wrap(JExpr expr) {
    return new VoidPtr(expr);
  }
}
