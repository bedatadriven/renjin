package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

import static org.renjin.gcc.codegen.expr.Expressions.newArray;

/**
 * Strategy for pointer types that uses a combination of an array value and an offset value
 */
public class FatPtrStrategy implements PointerTypeStrategy<FatPtrExpr> {

  private ValueFunction valueFunction;
  private boolean parametersWrapped = true;

  /**
   * The JVM type of the array used to back the pointer
   */
  private Type arrayType; 

  public FatPtrStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
    this.arrayType = Type.getType("[" + valueFunction.getValueType().getDescriptor());
  }

  public boolean isParametersWrapped() {
    return parametersWrapped;
  }

  public FatPtrStrategy setParametersWrapped(boolean parametersWrapped) {
    this.parametersWrapped = parametersWrapped;
    return this;
  }

  @Override
  public FatPtrExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    if(decl.isAddressable()) {
      // If this variable needs to be addressable, then we need to store in a unit length pointer
      // so that we can later get its "address"
      // For example, if creating a double pointer variable that needs to be later:
      
      // C:
      //
      // void init(double **pp) {
      //   double *p = malloc(3 * sizeof(double));
      //   p[1] = 42.0;
      //   p[2] = 33.4;
      //   *pp = p+1;
      // }      
      // 
      // void test() {
      //   double *p;
      //   init(&p)
      //   double x = *p + *(p+1)
      // }

      
      // The solution is to store the pointer as unit-length array of wrappers. Then we can pass this 
      // to other methods and allow them to set the array and offset
      
      // void init(ObjectPtr pp) {
      //   double p[] = new double[3];
      //   int p$offset = 0;
      //   p[p$offset + 1] = 42.0;
      //   p[p$offset + 2] = 33.4;
      //   pp.array[pp.offset] = new DoublePtr(p, p$offset)
      // }      
      // 
      // void test() {
      //   DoublePtr[] p = new DoublePtr[] { new DoublePtr() };
      //   init(new ObjectPtr(p, 0));
      //   double x = p.array[p.offset] + p.array[p.offset+1]
      // }

      Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
      Type wrapperArrayType = Wrappers.valueArrayType(wrapperType);
      
      FatPtrExpr nullPtr = FatPtrExpr.nullPtr(valueFunction);
      
      SimpleLValue unitArray = allocator.reserve(decl.getName(), wrapperArrayType, newArray(nullPtr.wrap()));
      FatPtrExpr address = new FatPtrExpr(unitArray); 
      SimpleExpr instance = Expressions.elementAt(unitArray, 0);
      SimpleExpr unwrappedArray = Wrappers.arrayField(instance, valueFunction.getValueType());
      SimpleExpr unwrappedOffset = Wrappers.offsetField(instance);      
      return new FatPtrExpr(address, unwrappedArray, unwrappedOffset);

    } else {
      SimpleLValue array = allocator.reserve(decl.getName(), arrayType);
      SimpleLValue offset = allocator.reserveInt(decl.getName() + "$offset");

      return new FatPtrExpr(array, offset);
    }
  }

  @Override
  public FatPtrExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy fieldGenerator(String className, String fieldName) {
    return new FatPtrFieldStrategy(valueFunction, fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(String className, String fieldName) {
    return new AddressableField(Type.getType(className), fieldName, new FatPtrValueFunction(valueFunction));
  }

  @Override
  public ParamStrategy getParamStrategy() {
    if(isParametersWrapped()) {
      return new WrappedFatPtrParamStrategy(valueFunction);
    } else {
      return new FatPtrParamStrategy(valueFunction);
    }
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new FatPtrReturnStrategy(valueFunction);
  }

  @Override
  public FatPtrExpr malloc(MethodGenerator mv, SimpleExpr length) {
    return FatPtrMalloc.alloc(mv, valueFunction, length);
  }

  @Override
  public FatPtrExpr realloc(FatPtrExpr pointer, SimpleExpr length) {
    SimpleExpr array = new FatPtrRealloc(pointer, length);
    SimpleExpr offset = Expressions.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public Expr valueOf(FatPtrExpr pointerExpr) {
    return valueFunction.dereference(pointerExpr.getArray(), pointerExpr.getOffset());
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new FatPtrValueFunction(valueFunction));
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return new ArrayTypeStrategy(arrayType, new FatPtrValueFunction(valueFunction));
  }

  @Override
  public FatPtrExpr pointerPlus(FatPtrExpr pointer, SimpleExpr offsetInBytes) {
    int bytesPerArrayElement = valueFunction.getElementSize() / valueFunction.getElementLength();
    SimpleExpr offsetInArrayElements = Expressions.divide(offsetInBytes, bytesPerArrayElement);
    SimpleExpr newOffset = Expressions.sum(pointer.getOffset(), offsetInArrayElements);
    
    return new FatPtrExpr(pointer.getArray(), newOffset);
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, FatPtrExpr x, FatPtrExpr y) {
    return new FatPtrConditionGenerator(op, x, y);
  }

  @Override
  public SimpleExpr memoryCompare(FatPtrExpr p1, FatPtrExpr p2, SimpleExpr n) {
    return new FatPtrMemCmp(p1, p2, n);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, FatPtrExpr destination, FatPtrExpr source, SimpleExpr length) {
    
    // TODO: Is this correct for pointers to record types?
    
    source.getArray().load(mv);
    source.getOffset().load(mv);
    destination.getArray().load(mv);
    destination.getOffset().load(mv);
    length.load(mv);

    // public static native void arraycopy(
    //     Object src,  int  srcPos,
    // Object dest, int destPos,
    // int length);
    mv.invokestatic(System.class, "arraycopy", 
        Type.getMethodDescriptor(Type.VOID_TYPE, 
              Type.getType(Object.class), Type.INT_TYPE, 
              Type.getType(Object.class), Type.INT_TYPE,
              Type.INT_TYPE));

  }

  @Override
  public FatPtrExpr nullPointer() {
    return FatPtrExpr.nullPtr(valueFunction);
  }
}
