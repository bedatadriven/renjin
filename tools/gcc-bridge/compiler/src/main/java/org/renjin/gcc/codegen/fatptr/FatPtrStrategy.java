package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.voidt.VoidPtrStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.type.GimpleArrayType;

import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
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
  
  public Type getWrapperType() {
    return Wrappers.wrapperType(valueFunction.getValueType());
  }
  
  public Type getArrayType() {
    return Wrappers.valueArrayType(valueFunction.getValueType());
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
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    return new FatPtrFieldStrategy(valueFunction, fieldName);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return new AddressableField(className, fieldName, new FatPtrValueFunction(valueFunction));
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
  public FatPtrExpr malloc(MethodGenerator mv, SimpleExpr sizeInBytes) {
    SimpleExpr length = Expressions.divide(sizeInBytes, valueFunction.getArrayElementBytes());
    
    return FatPtrMalloc.alloc(mv, valueFunction, length);
  }

  @Override
  public FatPtrExpr realloc(FatPtrExpr pointer, SimpleExpr newSizeInBytes) {
    SimpleExpr sizeInElements = Expressions.divide(newSizeInBytes, valueFunction.getArrayElementBytes());
    SimpleExpr array = new FatPtrRealloc(pointer, sizeInElements);
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
  public FatPtrExpr cast(Expr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof VoidPtrStrategy) {
      SimpleExpr wrapperInstance = Wrappers.cast(valueFunction.getValueType(), (SimpleExpr) value);

      SimpleExpr arrayField = Wrappers.arrayField(wrapperInstance);
      SimpleExpr offsetField = Wrappers.offsetField(wrapperInstance);

      return new FatPtrExpr(arrayField, offsetField);
    
    } else if(typeStrategy instanceof FatPtrStrategy) {
      // allow any casts between FatPtrs. though runtime errors may occur
      // (The JVM simply won't allow us to cast an int* to a double*)
      FatPtrExpr ptrExpr = (FatPtrExpr) value;
      Expr address = null;
      if(ptrExpr.isAddressable()) {
        address = ptrExpr.addressOf();
      }
      SimpleExpr castedArray = Expressions.uncheckedCast(ptrExpr.getArray(), arrayType);
      SimpleExpr offset = ptrExpr.getOffset();
      
      return new FatPtrExpr(address, castedArray, offset);
    }
    
    throw new UnsupportedCastException();
  }

  @Override
  public FatPtrExpr pointerPlus(FatPtrExpr pointer, SimpleExpr offsetInBytes) {
    SimpleExpr offsetInArrayElements = Expressions.divide(offsetInBytes, valueFunction.getArrayElementBytes());
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
  public void memoryCopy(MethodGenerator mv, FatPtrExpr destination, FatPtrExpr source, SimpleExpr lengthBytes) {
    
    // TODO: Is this correct for pointers to record types?
    
    // Convert bytes -> array elements
    SimpleExpr length = Expressions.divide(lengthBytes, valueFunction.getArrayElementBytes());
    
    // Push parameters onto stack
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
        Type.getMethodDescriptor(VOID_TYPE, 
              Type.getType(Object.class), INT_TYPE, 
              Type.getType(Object.class), INT_TYPE,
              INT_TYPE));

  }

  @Override
  public void memorySet(MethodGenerator mv, FatPtrExpr pointer, SimpleExpr byteValue, SimpleExpr length) {
    
    // Each of the XXXPtr classes have a static memset() method in the form:
    // void memset(double[] str, int strOffset, int c, int n)

    Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());

    String methodDescriptor = Type.getMethodDescriptor(VOID_TYPE, arrayType, INT_TYPE, INT_TYPE, INT_TYPE);

    pointer.getArray().load(mv);
    pointer.getOffset().load(mv);
    byteValue.load(mv);
    length.load(mv);
    
    mv.invokestatic(wrapperType, "memset", methodDescriptor);
  }

  @Override
  public SimpleExpr toVoidPointer(FatPtrExpr ptrExpr) {
    return ptrExpr.wrap();
  }


  @Override
  public FatPtrExpr nullPointer() {
    return FatPtrExpr.nullPtr(valueFunction);
  }

  @Override
  public FatPtrExpr unmarshallVoidPtrReturnValue(MethodGenerator mv, SimpleExpr voidPointer) {

    // cast the result to the wrapper type, e.g. ObjectPtr or DoublePtr
    Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
    SimpleExpr wrapperPtr = Wrappers.cast(valueFunction.getValueType(), voidPointer);

    // Reserve a local variable to hold the result
    SimpleLValue retVal = mv.getLocalVarAllocator().reserve("_retval", wrapperType);

    // store the result of the call to the temp variable
    retVal.store(mv, wrapperPtr);

    // Now unpack the array and offset into seperate local variables
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    SimpleLValue arrayVar = mv.getLocalVarAllocator().reserve("_retval$array", arrayType);
    SimpleLValue offsetVar = mv.getLocalVarAllocator().reserveInt("_retval$offset");
    
    arrayVar.store(mv, Wrappers.arrayField(retVal, valueFunction.getValueType()));
    offsetVar.store(mv, Wrappers.offsetField(retVal));
    
    return new FatPtrExpr(arrayVar, offsetVar);
  }

}
