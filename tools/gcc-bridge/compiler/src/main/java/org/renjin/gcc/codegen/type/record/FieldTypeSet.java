package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.asm.Type;

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

  public FieldTypeSet(List<GimpleRecordTypeDef> unions, List<GimpleRecordTypeDef> records) {

    // Add the non-record fields from the unions themselves
    for (GimpleRecordTypeDef union : unions) {
      for (GimpleField gimpleField : union.getFields()) {
        if(!(gimpleField.getType() instanceof GimpleRecordType)) {
          addField(gimpleField);
        }
      }
    }

    // Add the field of the member record types
    for (GimpleRecordTypeDef record : records) {
      for (GimpleField gimpleField : record.getFields()) {
        if(!isCircularField(record, gimpleField)) {
          addField(gimpleField);
        }
      }
    }
  }

  private void addField(GimpleField field) {
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
    return getValueTypes().size() == 1;
  }

  private Set<Type> getValueTypes() {
    return Sets.union(valueTypes, addressableValueTypes);
  }

  public Type uniqueValueType() {
    return Iterables.getOnlyElement(getValueTypes());
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


  private static GimpleType findUltimateComponentType(GimpleArrayType arrayType) {
    if(arrayType.getComponentType() instanceof GimpleArrayType) {
      return findUltimateComponentType((GimpleArrayType) arrayType.getComponentType());
    } else if(arrayType.getComponentType() instanceof GimpleComplexType) {
      return ((GimpleComplexType) arrayType.getComponentType()).getPartType();
    } else {
      return arrayType.getComponentType();
    }
  }


}
