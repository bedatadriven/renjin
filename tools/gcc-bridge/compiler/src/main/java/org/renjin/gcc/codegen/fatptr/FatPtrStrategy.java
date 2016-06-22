package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
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
      
      JLValue unitArray = allocator.reserve(decl.getName(), wrapperArrayType, newArray(nullPtr.wrap()));
      FatPtrExpr address = new FatPtrExpr(unitArray); 
      JExpr instance = Expressions.elementAt(unitArray, 0);
      JExpr unwrappedArray = Wrappers.arrayField(instance, valueFunction.getValueType());
      JExpr unwrappedOffset = Wrappers.offsetField(instance);      
      return new FatPtrExpr(address, unwrappedArray, unwrappedOffset);

    } else {
      JLValue array = allocator.reserve(decl.getNameIfPresent(), arrayType);
      JLValue offset = allocator.reserveOffsetInt(decl.getNameIfPresent());

      return new FatPtrExpr(array, offset);
    }
  }
  
  public PrimitiveValue toInt(FatPtrExpr fatPtrExpr) {
    // Converting pointers to integers and vice-versa is implementation-defined
    // So we will define an implementation that supports at least one useful case spotted in S4Vectors:
    // double a[] = {1,2,3,4};
    // double *start = a;
    // double *end = p+4;
    // int length = (start-end)
    JExpr offset = fatPtrExpr.getOffset();
    JExpr offsetInBytes = Expressions.product(offset, valueFunction.getArrayElementBytes());

    return new PrimitiveValue(offsetInBytes);
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
  public FatPtrExpr malloc(MethodGenerator mv, JExpr sizeInBytes) {
    JExpr length = Expressions.divide(sizeInBytes, valueFunction.getArrayElementBytes());
    
    return FatPtrMalloc.alloc(mv, valueFunction, length);
  }

  @Override
  public FatPtrExpr realloc(FatPtrExpr pointer, JExpr newSizeInBytes) {
    JExpr sizeInElements = Expressions.divide(newSizeInBytes, valueFunction.getArrayElementBytes());
    JExpr array = new FatPtrRealloc(pointer, sizeInElements);
    JExpr offset = Expressions.zero();
    
    return new FatPtrExpr(array, offset);
  }

  @Override
  public GExpr valueOf(FatPtrExpr pointerExpr) {
    return valueFunction.dereference(pointerExpr.getArray(), pointerExpr.getOffset());
  }

  @Override
  public FatPtrStrategy pointerTo() {
    return new FatPtrStrategy(new FatPtrValueFunction(valueFunction));
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, new FatPtrValueFunction(valueFunction));
  }

  @Override
  public FatPtrExpr cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof VoidPtrStrategy) {
      VoidPtr ptrExpr = (VoidPtr) value;
      JExpr wrapperInstance = Wrappers.cast(valueFunction.getValueType(), ptrExpr.unwrap());

      JExpr arrayField = Wrappers.arrayField(wrapperInstance);
      JExpr offsetField = Wrappers.offsetField(wrapperInstance);

      return new FatPtrExpr(arrayField, offsetField);
    
    } else if(typeStrategy instanceof FatPtrStrategy) {
      // allow any casts between FatPtrs. though runtime errors may occur
      // (The JVM simply won't allow us to cast an int* to a double*)
      FatPtrExpr ptrExpr = (FatPtrExpr) value;
      GExpr address = null;
      if (ptrExpr.isAddressable()) {
        address = ptrExpr.addressOf();
      }
      JExpr castedArray = Expressions.uncheckedCast(ptrExpr.getArray(), arrayType);
      JExpr offset = ptrExpr.getOffset();

      return new FatPtrExpr(address, castedArray, offset);

    } else if(typeStrategy instanceof RecordUnitPtrStrategy) {
      RecordUnitPtr ptr = (RecordUnitPtr) value;
      JExpr ref = Expressions.cast(ptr.unwrap(), valueFunction.getValueType());
      JExpr newArray = Expressions.newArray(ref);
      
      return new FatPtrExpr(newArray);
    }
    
    throw new UnsupportedCastException();
  }

  @Override
  public FatPtrExpr pointerPlus(FatPtrExpr pointer, JExpr offsetInBytes) {
    JExpr offsetInArrayElements = Expressions.divide(offsetInBytes, valueFunction.getArrayElementBytes());
    JExpr newOffset = Expressions.sum(pointer.getOffset(), offsetInArrayElements);
    
    return new FatPtrExpr(pointer.getArray(), newOffset);
  }

  @Override
  public ConditionGenerator comparePointers(GimpleOp op, FatPtrExpr x, FatPtrExpr y) {
    return new FatPtrConditionGenerator(op, x, y);
  }

  @Override
  public JExpr memoryCompare(FatPtrExpr p1, FatPtrExpr p2, JExpr n) {
    return new FatPtrMemCmp(p1, p2, n);
  }

  @Override
  public void memoryCopy(MethodGenerator mv, FatPtrExpr destination, FatPtrExpr source, JExpr lengthBytes, boolean buffer) {
    
    // Convert bytes -> value counts
    JExpr valueCount = Expressions.divide(lengthBytes, valueFunction.getArrayElementBytes());
    
    valueFunction.memoryCopy(mv,
        destination.getArray(), destination.getOffset(),
        source.getArray(), source.getOffset(), valueCount);
  }

  @Override
  public void memorySet(MethodGenerator mv, FatPtrExpr pointer, JExpr byteValue, JExpr length) {
    
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
  public VoidPtr toVoidPointer(FatPtrExpr ptrExpr) {
    return new VoidPtr(ptrExpr.wrap());
  }


  @Override
  public FatPtrExpr nullPointer() {
    return FatPtrExpr.nullPtr(valueFunction);
  }

  @Override
  public FatPtrExpr unmarshallVoidPtrReturnValue(MethodGenerator mv, JExpr voidPointer) {

    // cast the result to the wrapper type, e.g. ObjectPtr or DoublePtr
    Type wrapperType = Wrappers.wrapperType(valueFunction.getValueType());
    JExpr wrapperPtr = Wrappers.cast(valueFunction.getValueType(), voidPointer);

    // Reserve a local variable to hold the result
    JLValue retVal = mv.getLocalVarAllocator().reserve(wrapperType);

    // store the result of the call to the temp variable
    retVal.store(mv, wrapperPtr);

    // Now unpack the array and offset into seperate local variables
    Type arrayType = Wrappers.valueArrayType(valueFunction.getValueType());
    JLValue arrayVar = mv.getLocalVarAllocator().reserve(arrayType);
    JLValue offsetVar = mv.getLocalVarAllocator().reserve(Type.INT_TYPE);
    
    arrayVar.store(mv, Wrappers.arrayField(retVal, valueFunction.getValueType()));
    offsetVar.store(mv, Wrappers.offsetField(retVal));
    
    return new FatPtrExpr(arrayVar, offsetVar);
  }

  @Override
  public String toString() {
    return "FatPtrStrategy[" + valueFunction + "]";
  }
}
