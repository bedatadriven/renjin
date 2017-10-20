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

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.fun.FunPtrField;
import org.renjin.gcc.codegen.type.primitive.PrimitiveFieldStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtrValueFunction;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RecordClassLayout implements RecordLayout {

  public static class Field {
    private RecordClassLayoutTree.Node node;
    private FieldStrategy strategy;

    public Field(RecordClassLayoutTree.Node node, FieldStrategy strategy) {
      this.node = node;
      this.strategy = strategy;
    }

    public FieldStrategy getStrategy() {
      return strategy;
    }

    public int getOffsetInBytes() {
      return node.getOffset() / 8;
    }

    public int getSizeInBytes() {
      return node.getSize() / 8;
    }
  }
  
  private RecordClassLayoutTree tree;
  private UnionSet unionSet;
  private Type type;

  private List<Field> fields;
  
  /**
   * Maps an offset to a FieldStrategy.
   */
  private TreeMap<Integer, FieldStrategy> fieldMap = new TreeMap<>();

  private Set<String> fieldNames = new HashSet<>();
  

  public RecordClassLayout(UnionSet unionSet, Type type) {
    this.unionSet = unionSet;
    this.type = type;
    this.tree = new RecordClassLayoutTree(unionSet);
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    fields = new ArrayList<>();
    for (RecordClassLayoutTree.Node node : tree.getTree()) {
      Field field = new Field(node, buildStrategy(typeOracle, node));
      fields.add(field);
      fieldMap.put(node.getOffset(), field.getStrategy());
    }
  }

  private FieldStrategy buildStrategy(TypeOracle typeOracle, RecordClassLayoutTree.Node node) {
    FieldTypeSet typeSet = node.typeSet();
    if(typeSet.getGimpleTypes().size() == 1) {
      TypeStrategy typeStrategy = typeOracle.forType(typeSet.getGimpleTypes().iterator().next());

      if(isPotentialSuperClass(node, typeStrategy)) {
        return new SuperClassFieldStrategy((RecordClassTypeStrategy) typeStrategy);
      }

      if(node.isAddressable()) {
        return typeStrategy.addressableFieldGenerator(type, uniqueFieldName(node));
      } else {
        return typeStrategy.fieldGenerator(type, uniqueFieldName(node));
      }

    } else if(typeSet.allPointersToPrimitives()) {
      if (node.isAddressable()) {
        return new AddressableField(type, uniqueFieldName(node), new VoidPtrValueFunction());
      } else {
        return new PrimitivePointerUnionField(type, uniqueFieldName(node));
      }

    } else if(typeSet.allFunctionPointers()) {
      return new FunPtrField(type, uniqueFieldName(node));
      
    } else if(typeSet.allPointers()) {
      if(node.isAddressable()) {
        return new AddressableField(type, uniqueFieldName(node), new VoidPtrValueFunction());
      } else {
        return new PointerUnionField(type, uniqueFieldName(node));
      }
  
    } else if(typeSet.allPrimitives()) {
      Optional<Type> commonType = typeSet.tryComputeCommonType();
      if(!commonType.isPresent()) {
        throw new UnsupportedOperationException("No common type possible for fields: " + node.getFields());
      }
      return new PrimitiveFieldStrategy(type, uniqueFieldName(node), GimplePrimitiveType.fromJvmType(commonType.get()));

    } else {
      Optional<Type> commonType = typeSet.tryComputeCommonType();
      if(commonType.isPresent()) {
        int arrayLength = node.getSize() / GimplePrimitiveType.fromJvmType(commonType.get()).getSize();
        return new RecordArrayField(type, uniqueFieldName(node), commonType.get(), arrayLength, new GimpleRecordType(this.unionSet.getAllTypes().iterator().next()));
      }

      Optional<GimpleRecordType> commonRecordType = typeSet.tryFindCommonRecordType();
      if(commonRecordType.isPresent()) {
        int arrayLength = node.getSize() / commonRecordType.get().getSize();
        GimpleArrayType arrayType = new GimpleArrayType(commonRecordType.get(), arrayLength);
        RecordTypeStrategy recordTypeStrategy = (RecordTypeStrategy) typeOracle.forType(commonRecordType.get());
        return recordTypeStrategy.arrayOf(arrayType).fieldGenerator(type, uniqueFieldName(node));
      }

      throw new UnsupportedOperationException("TODO: " + unionSet.debugString());
    }
  }

  private String uniqueFieldName(RecordClassLayoutTree.Node node) {
    String fieldName = node.name();
    
    // Remove illegal characters
    // http://stackoverflow.com/questions/30491035/can-java-class-files-use-reserved-keywords-as-names
    fieldName = fieldName
                .replace('.', '$')
                .replace(';', '$')
                .replace('[', '$');
    
    if(fieldName.isEmpty()) {
      return "$offset" + node.getOffset();
    }
    if(fieldNames.contains(fieldName)) {
      return fieldName + "$" + node.getOffset();
    } else {
      return fieldName;
    }
  }

  private boolean isPotentialSuperClass(RecordClassLayoutTree.Node node, TypeStrategy strategy) {

    if(node.getOffset() == 0) {
      if (strategy instanceof RecordClassTypeStrategy) {
        RecordClassTypeStrategy recordStrategy = (RecordClassTypeStrategy) strategy;
        if (!recordStrategy.getJvmType().equals(this.type)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Type getType() {
    return type;
  }


  @Override
  public GExpr memberOf(MethodGenerator mv, RecordValue instance, int offset, int size, GimpleType type) {
    
    // If this field is a unioned record type, then return a pointer to ourselves
    if(offset == 0 && type instanceof RecordClassTypeStrategy &&
        ((RecordClassTypeStrategy) type).getJvmType().equals(this.type)) {
      return instance;
    }

    JExpr instanceRef = Expressions.cast(instance.unwrap(), this.type);
    JExpr instanceVar = mv.getLocalVarAllocator().tempIfNeeded(mv, instanceRef);

    // Find the logical field that contains this bit range
    Integer fieldStart = fieldMap.floorKey(offset);
    if(fieldStart == null) {
      throw new IllegalStateException("No field declared at offset " + offset);
    }
    FieldStrategy fieldStrategy = fieldMap.get(fieldStart);

    if(fieldStrategy == null) {
      throw new IllegalStateException(this.type + " has no field at offset " + offset);
    }
    
    return fieldStrategy.memberExpr(mv, instanceVar, offset - fieldStart, size, type);
  }

  @Override
  public RecordValue clone(MethodGenerator mv, RecordValue recordValue) {
    return recordValue.doClone(mv);
  }

  @Override
  public void writeClassFiles(File outputDir) throws IOException {
    RecordClassGenerator generator = new RecordClassGenerator(type, getSuperClass(), fields, 
        unionSet.sizeOf());
    generator.writeClassFile(outputDir);
  }

  private Type getSuperClass() {
    for (FieldStrategy fieldStrategy : fieldMap.values()) {
      if(fieldStrategy instanceof SuperClassFieldStrategy) {
        return ((SuperClassFieldStrategy) fieldStrategy).getType();
      }
    }
    return Type.getType(Object.class);
  }
  
}
