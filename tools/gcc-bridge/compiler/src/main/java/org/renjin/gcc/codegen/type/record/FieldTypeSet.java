/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Sets;

import javax.swing.text.html.Option;
import java.util.HashSet;
import java.util.Iterator;
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
    } else if(type instanceof GimpleOffsetType) {
      // TODO

    } else {
      throw new IllegalStateException("type: " + type);
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

      Set<Type> dissonantTypes = Sets.difference(valueTypes, addressableValueTypes);
      if(dissonantTypes.isEmpty()) {
        return Optional.of(type);
      } else {
        return Optional.absent();
      }
    }

    // If we have exactly one value type, that's the obvious choice for the 
    // element size
    if(recordTypes.isEmpty() && valueTypes.size() == 1) {
      return Optional.of(Iterables.getOnlyElement(valueTypes));
    }

    return Optional.absent();
  }

  public Optional<GimpleRecordType> tryFindCommonRecordType() {
    if( pointerTypes.size() > 0 ||
        valueTypes.size() > 0 ||
        addressableValueTypes.size() > 0) {

      return Optional.absent();
    }

    if(recordTypes.size() == 1) {
      return Optional.of(Iterables.getOnlyElement(recordTypes));
    }

    return Optional.absent();
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

  public boolean allFunctionPointers() {
    for (GimpleType gimpleType : gimpleTypes) {
      if(!gimpleType.isPointerTo(GimpleFunctionType.class)) {
        return false;
      }
    }
    return true;
  }

  public boolean allPointers() {
    for (GimpleType gimpleType : gimpleTypes) {
      if(!(gimpleType instanceof GimpleIndirectType)) {
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
