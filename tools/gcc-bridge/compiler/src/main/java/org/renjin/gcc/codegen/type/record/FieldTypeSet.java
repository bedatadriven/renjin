package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.renjin.gcc.codegen.type.record.RecordTypeStrategy.isCircularField;

/**
 * Set of field types that are present in one or more 
 * RecordTypeDefs. 
 */
public class FieldTypeSet {

  private Set<Type> addressableValueTypes = new HashSet<>();
  private Set<Type> valueTypes = new HashSet<>();
  private Set<GimpleIndirectType> pointerTypes = Sets.newHashSet();
  private Set<GimpleRecordType> recordTypes = Sets.newHashSet();

  private Set<GimpleType> gimpleTypes = new HashSet<>();
  
  public FieldTypeSet(List<GimpleRecordTypeDef> unions, List<GimpleRecordTypeDef> records) {

    // Add the non-record fields from the unions themselves
    for (GimpleRecordTypeDef union : unions) {
      for (GimpleField field : union.getFields()) {
        if(!(field.getType() instanceof GimpleRecordType)) {
          addField(field);
        }
      }
    }

    // Add the field of the member record types
    for (GimpleRecordTypeDef record : records) {
      for (GimpleField field : record.getFields()) {
        if(!isCircularField(record, field)) {
          addField(field);
        }
      }
    }
  }
  
  public FieldTypeSet(Iterable<GimpleField> fields) {
    for (GimpleField field : fields) {
      addField(field);
    }
  }

  private void addField(GimpleField field) {
    gimpleTypes.add(field.getType());
    
    if (field.getType() instanceof GimpleArrayType) {
      GimpleType componentType = findUltimateComponentType((GimpleArrayType) field.getType());
      addType(componentType, true);

    } else {
      addType(field.getType(), field.isAddressed());
    }
  }

  private void addType(GimpleType type, boolean addressable) {
    if(type instanceof GimplePrimitiveType) {
      Type valueType = ((GimplePrimitiveType) type).jvmType();
      if (addressable) {
        addressableValueTypes.add(valueType);
      } else {
        valueTypes.add(valueType);
      }
      
    } else if(type instanceof GimpleComplexType) {
      Type valueType = ((GimpleComplexType) type).getJvmPartType();
      if(addressable) {
        addressableValueTypes.add(valueType);
      } else {
        valueTypes.add(valueType);
      }
      
    } else if(type instanceof GimpleIndirectType) {
      pointerTypes.add((GimpleIndirectType) type);

    } else if(type instanceof GimpleRecordType) {
      recordTypes.add((GimpleRecordType) type);

    } else {
      throw new IllegalThreadStateException("type: " + type);
    }
  }

  public boolean isEmpty() {
    return valueTypes.isEmpty() && addressableValueTypes.isEmpty() && pointerTypes.isEmpty() &&
        recordTypes.isEmpty();
  }

  /**
   * @return true if the best representation for this UnionSet is an array of primitives.
   */
  public boolean isBestRepresentableAsArray() {
    if(!pointerTypes.isEmpty() || !recordTypes.isEmpty()) {
      return false;
    }
    return getPrimitiveTypes().size() == 1;
  }

  public Set<GimpleType> getGimpleTypes() {
    return gimpleTypes;
  }
  
  public Set<Type> getPrimitiveTypes() {
    return Sets.union(valueTypes, addressableValueTypes);
  }

  
  public Type uniquePrimitiveType() {
    return Iterables.getOnlyElement(getPrimitiveTypes());
  }

  /**
   * Tries to compute a common type to use for all fields so that we can represent this
   * type or union of types as an array.
   */
  public Optional<Type> tryComputeCommonType() {

    // We can't mix pointer types and primitive types in the same array;
    // the JVM needs to know where to find pointers!
    if(!pointerTypes.isEmpty()) {
      return Optional.absent();
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

  private Type widestType() {

    // Otherwise return the largest type, favoring integer types 
    int maxSize = 0;
    for (Type valueType : valueTypes) {
      switch (valueType.getSort()) {
        case Type.LONG:
        case Type.DOUBLE:
          maxSize = 64;
          break;
        case Type.FLOAT:
        case Type.INT:
          maxSize = Math.max(maxSize, 32);
          break;
        case Type.SHORT:
        case Type.CHAR:
          maxSize = Math.max(maxSize, 16);
          break;
        case Type.BYTE:
        case Type.BOOLEAN:
          maxSize = Math.max(maxSize, 8);
          break;
      }
    }

    switch (maxSize) {
      case 64:
        return Type.LONG_TYPE;
      default:
        return Type.INT_TYPE;
    }
  }

  private static GimpleType findUltimateComponentType(GimpleArrayType arrayType) {
    if(arrayType.getComponentType() instanceof GimpleArrayType) {
      return findUltimateComponentType((GimpleArrayType) arrayType.getComponentType());
    } else if(arrayType.getComponentType() instanceof GimpleComplexType) {
      return ((GimpleComplexType) arrayType.getComponentType()).getPartType();
    } else {
      return arrayType.getComponentType();
    }
  }


  public boolean allPointersToPrimitives() {
    for (GimpleType gimpleType : gimpleTypes) {
      if(!gimpleType.isPointerTo(GimplePrimitiveType.class)) {
        return false;
      }
    }
    return true;
  }


  public boolean allPointers() {
    for (GimpleType gimpleType : gimpleTypes) {
      if(!(gimpleType instanceof GimplePointerType)) {
        return false;
      }
    }
    return true;
  }

  public boolean allPrimitives() {
    for (GimpleType gimpleType : gimpleTypes) {
      if(!(gimpleType instanceof GimplePrimitiveType)) {
        return false;
      }
    }
    return true;
  }

}
