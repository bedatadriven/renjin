package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.array.ArrayTypeStrategy;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrExpr;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
  
  public RecordArrayTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    super(recordTypeDef);
    fieldType = computeElementType(recordTypeDef).get();
    arrayType = Wrappers.valueArrayType(fieldType);
    arrayLength = computeArrayLength(recordTypeDef, fieldType);
    valueFunction = new RecordArrayValueFunction(fieldType, arrayLength);
  }


  /**
   * Returns true if the given {@code recordTypeDef} can be compiled using this strategy.
   */
  public static boolean accept(GimpleRecordTypeDef recordTypeDef) {
    return computeElementType(recordTypeDef).isPresent();
  }
  
  private static Optional<Type> computeElementType(GimpleRecordTypeDef typeDef) {
    Set<Type> addressableValueTypes = new HashSet<>();
    Set<Type> valueTypes = new HashSet<>();
    
    // Enumerate the different primitives that are part of this 
    // record layout, either as arrays or as individual values.
    
    for (GimpleField field : typeDef.getFields()) {
      if(isCircularField(typeDef, field)) {
        // ignore
        
      } else if(field.getType() instanceof GimpleArrayType) {
        GimpleType componentType = findUltimateComponentType((GimpleArrayType) field.getType());
        if(componentType instanceof GimplePrimitiveType) {
          addressableValueTypes.add(((GimplePrimitiveType) componentType).jvmType());
        } else {
          return Optional.absent();
        }
      
      } else if(field.getType() instanceof GimplePrimitiveType) {
        Type valueType = ((GimplePrimitiveType) field.getType()).jvmType();
        if(field.isAddressed()) {
          addressableValueTypes.add(valueType);
        } else {
          valueTypes.add(valueType);
        }
      } else {
        return Optional.absent();
      }
    }
    
    // We can never cast a double[] to a float[], so bail if we have arrays or 
    // addressable values of different types
    if(addressableValueTypes.size() > 1) {
      return Optional.absent();
      
    } else if(addressableValueTypes.size() == 1) {
      Type type = Iterables.getOnlyElement(addressableValueTypes);
      
      // Check to see if we've implemented the casting yet
      Set<Type> dissonantTypes = Sets.difference(valueTypes, addressableValueTypes);
      for (Type dissonantType : dissonantTypes) {
        if(!castingSupported(type, dissonantType)) {
          return Optional.absent();
        }
      }
      
      return Optional.of(type);
      
    }
    
    // If we have exactly one value type, that's the obvious choice for the 
    // element size
    if(valueTypes.size() == 1) {
      return Optional.of(Iterables.getOnlyElement(valueTypes));
    }
    return Optional.absent();
  }

  private static boolean castingSupported(Type arrayElementType, Type valueType) {
    if(arrayElementType.equals(Type.BYTE_TYPE) && valueType.equals(Type.INT_TYPE)) {
      return true;
    }
    return false;
  }


  private static GimpleType findUltimateComponentType(GimpleArrayType arrayType) {
    if(arrayType.getComponentType() instanceof GimpleArrayType) {
      return findUltimateComponentType((GimpleArrayType) arrayType.getComponentType());
    } else {
      return arrayType.getComponentType();
    }
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
  public Expr memberOf(RecordArrayExpr instance, GimpleFieldRef fieldRef) {
    
    // All the fields in this record are necessarily primitives, so we need
    // simple to retrieve the element from within the array that corresponds to
    // the given field name
    SimpleExpr array = instance.getArray();
    SimpleExpr fieldOffset = constantInt(fieldRef.getOffsetBytes() / elementSizeInBytes());
    SimpleExpr offset = sum(instance.getOffset(), fieldOffset);

    // Because this value is backed by an array, we can also make it addressable. 
    FatPtrExpr address = new FatPtrExpr(array, offset);
    
    // The members of this record may be either primitives, or arrays of primitives,
    // and it actually doesn't matter to us.
    
    if(fieldRef.getType() instanceof GimplePrimitiveType) {
      Type expectedType = ((GimplePrimitiveType) fieldRef.getType()).jvmType();

      // Return a single primitive value
      if(expectedType.equals(fieldType)) {
        SimpleExpr value = elementAt(array, offset);
        return new SimpleAddressableExpr(value, address);
      } else if (expectedType.equals(Type.INT_TYPE) && fieldType.equals(Type.BYTE_TYPE)) {
        return new ByteArrayAsInt(array, offset);
        
      } else {
        throw new UnsupportedOperationException("TODO: " + fieldType + " -> " + expectedType);
      }

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

    SimpleExpr newArray = newArray(fieldType, arrayLength);
    SimpleLValue arrayVar = allocator.reserve(decl.getName(), arrayType, newArray);
    
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
    return new ArrayTypeStrategy(arrayType, valueFunction);
  }

}
