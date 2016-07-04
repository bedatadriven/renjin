package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.array.ArrayExpr;
import org.renjin.gcc.codegen.array.ArrayTypeStrategies;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValue;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;

import static org.renjin.gcc.codegen.expr.Expressions.*;

/**
 * Represents a record with a primitive array.
 * 
 * <p>This strategy only works for records that have only primitives fields of the same type. For example,
 * the C struct:</p>
 * <pre>
 *   struct point {
 *     double x;
 *     double y;
 *   }
 * </pre>
 * <p>Can be compiled using a simple {@code double[]} instead of a full-blown JVM class. This makes it 
 * easy to allow pointers to records of such types to be cast back and forth between {@code double*} pointers
 * (or {@code int*} etc.)</p>
 */
public class RecordArrayTypeStrategy extends RecordTypeStrategy<RecordArrayExpr> {
  
  private Type fieldType;
  private Type arrayType;
  private int arrayLength;
  private final RecordArrayValueFunction valueFunction;
  
  public RecordArrayTypeStrategy(GimpleRecordTypeDef recordTypeDef, Type fieldType) {
    super(recordTypeDef);
    this.fieldType = fieldType;
    arrayType = Wrappers.valueArrayType(fieldType);
    arrayLength = computeArrayLength(recordTypeDef, fieldType);
    valueFunction = new RecordArrayValueFunction(fieldType, arrayLength);
  }

  private static int computeArrayLength(GimpleRecordTypeDef recordTypeDef, Type fieldType) {
    int recordSize = recordTypeDef.getSize();
    int elementSize = GimplePrimitiveType.fromJvmType(fieldType).getSize();
    if(elementSize == 0) {
      throw new IllegalStateException("sizeof(" + fieldType + ") = 0");
    }
    return recordSize / elementSize;
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    // NOOP
  }

  @Override
  public void writeClassFiles(File outputDirectory) throws IOException {
    // NOOP
    // We don't use classes
  }

  @Override
  public GExpr memberOf(RecordArrayExpr instance, GimpleFieldRef fieldRef) {
    
    // All the fields in this record are necessarily primitives, so we need
    // simple to retrieve the element from within the array that corresponds to
    // the given field name
    JExpr array = instance.getArray();
    JExpr fieldOffset = constantInt(fieldRef.getOffsetBytes() / elementSizeInBytes());
    JExpr offset = sum(instance.getOffset(), fieldOffset);

    // Because this value is backed by an array, we can also make it addressable. 
    FatPtrExpr address = new FatPtrExpr(array, offset);
    
    // The members of this record may be either primitives, or arrays of primitives,
    // and it actually doesn't matter to us.
    
    if(fieldRef.getType() instanceof GimplePrimitiveType) {
      Type expectedType = ((GimplePrimitiveType) fieldRef.getType()).jvmType();

      // Return a single primitive value
      if(expectedType.equals(fieldType)) {
        JExpr value = elementAt(array, offset);
        return new PrimitiveValue(value, address);
      } else if (expectedType.equals(Type.INT_TYPE) && fieldType.equals(Type.BYTE_TYPE)) {
        return new PrimitiveValue(new ByteArrayAsInt(array, offset));
        
      } else {
        throw new UnsupportedOperationException("TODO: " + fieldType + " -> " + expectedType);
      }

    } else if(fieldRef.getType() instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) fieldRef.getType();
      Type expectedType = ((GimplePrimitiveType) arrayType.getComponentType()).jvmType();

      return new ArrayExpr(new PrimitiveValueFunction(expectedType), arrayType.getElementCount(), array, offset);
      
    } else {
      // Return an array that starts at this point 
      return new FatPtrExpr(address, array, offset);
    }
    
  }

  private int elementSizeInBytes() {
    return GimplePrimitiveType.fromJvmType(fieldType).sizeOf();
  }

  @Override
  public ParamStrategy getParamStrategy() {
    return new RecordArrayParamStrategy(arrayType, arrayLength);
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    return new RecordArrayReturnStrategy(arrayType, arrayLength);
  }

  @Override
  public RecordArrayExpr variable(GimpleVarDecl decl, VarAllocator allocator) {

    JExpr newArray = newArray(fieldType, arrayLength);
    JLValue arrayVar = allocator.reserve(decl.getName(), arrayType, newArray);
    
    return new RecordArrayExpr(arrayVar, arrayLength);
  }

  @Override
  public RecordArrayExpr constructorExpr(ExprFactory exprFactory, GimpleConstructor value) {
    return new RecordArrayExpr(newArray(fieldType, arrayLength), arrayLength);
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, final String fieldName) {
    return new RecordArrayField(className, fieldName, arrayType, arrayLength);
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    return fieldGenerator(className, fieldName);
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new FatPtrStrategy(valueFunction);
  }

  @Override
  public ArrayTypeStrategy arrayOf(GimpleArrayType arrayType) {
    return ArrayTypeStrategies.of(arrayType, valueFunction);
  }

  @Override
  public RecordArrayExpr cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    if(typeStrategy instanceof RecordArrayTypeStrategy) {
      return (RecordArrayExpr) value;
    }  else if(typeStrategy instanceof FatPtrStrategy) {
      FatPtrExpr fatPtrExpr = (FatPtrExpr) value;
      return new RecordArrayExpr(fatPtrExpr.getArray(), fatPtrExpr.getOffset(), arrayLength);
    }
    throw new UnsupportedCastException();
  }

  @Override
  public String toString() {
    return "RecordArrayTypeStrategy[" + valueFunction.getValueType() + "]";
  }
}
