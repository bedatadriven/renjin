package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.fatptr.AddressableField;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtrField;
import org.renjin.gcc.codegen.type.voidt.VoidPtrValueFunction;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecordClassLayout implements RecordLayout {

  private RecordClassLayoutTree tree;
  private UnionSet unionSet;
  private Type type;

  private Map<Integer, FieldStrategy> fields = new HashMap<>();

  private Set<String> fieldNames = new HashSet<>();
  

  public RecordClassLayout(UnionSet unionSet, Type type) {
    this.unionSet = unionSet;
    this.type = type;
    this.tree = new RecordClassLayoutTree(unionSet);
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    for (RecordClassLayoutTree.Node node : tree.getTree()) {
      fields.put(node.getOffset(), buildStrategy(typeOracle, node));
    }
  }

  private FieldStrategy buildStrategy(TypeOracle typeOracle, RecordClassLayoutTree.Node node) {
    FieldTypeSet typeSet = node.typeSet();
    if(typeSet.getGimpleTypes().size() == 1) {
      TypeStrategy typeStrategy = typeOracle.forType(typeSet.getGimpleTypes().iterator().next());

      if(isPotentialSuperClass(typeStrategy)) {
        return new SuperClassFieldStrategy((RecordClassTypeStrategy) typeStrategy);
      }

      if(node.isAddressable()) {
        return typeStrategy.addressableFieldGenerator(type, uniqueFieldName(node));
      } else {
        return typeStrategy.fieldGenerator(type, uniqueFieldName(node));
      }

    } else if(typeSet.allPointersToPrimitives()) {
      if (node.isAddressable()) {
        throw new UnsupportedOperationException("TODO");
      } else {
        return new PrimitivePointerUnionField(type, uniqueFieldName(node));
      }
   
    } else if(typeSet.allPointers()) {
      if(node.isAddressable()) {
        return new AddressableField(type, uniqueFieldName(node), new VoidPtrValueFunction());
      } else {
        return new VoidPtrField(uniqueFieldName(node));
      }
  
    } else if(typeSet.allPrimitives()) {
      Optional<Type> commonType = typeSet.tryComputeCommonType();
      if(!commonType.isPresent()) {
        throw new UnsupportedOperationException("No common type possible for fields: " + node.getFields());
      }
      return new PrimitiveUnionField(type, commonType.get(), uniqueFieldName(node));
      
    } else {
      throw new UnsupportedOperationException("TODO: " + unionSet.debugString());
    }
  }

  private String uniqueFieldName(RecordClassLayoutTree.Node node) {
    String fieldName = node.name();
    if(fieldNames.contains(fieldName)) {
      return fieldName + "$" + node.getOffset();
    } else {
      return fieldName;
    }
  }

  private boolean isPotentialSuperClass(TypeStrategy strategy) {
    if(strategy instanceof RecordClassTypeStrategy) {
      RecordClassTypeStrategy recordStrategy = (RecordClassTypeStrategy) strategy;
      if(recordStrategy.isUnitPointer()) {
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
  public GExpr memberOf(RecordValue instance, GimpleFieldRef fieldRef) {
    
    // If this field is a unioned record type, then return a pointer to ourselves
    if(isUnionMember(fieldRef)) {
      return instance;
    }
    
    FieldStrategy fieldStrategy = fields.get(fieldRef.getOffset());
    if(fieldStrategy == null) {
      throw new IllegalStateException(type + " has no field at offset " + fieldRef.getOffset());
    }
    return fieldStrategy.memberExpr(instance.unwrap(), 0, fieldRef.getType());
  }

  private boolean isUnionMember(GimpleFieldRef fieldRef) {
    if(fieldRef.getType() instanceof GimpleRecordType) {
      GimpleRecordType fieldType = (GimpleRecordType) fieldRef.getType();
      for (GimpleRecordTypeDef typeDef : unionSet.getRecords()) {
        if (typeDef.getId().equals(fieldType.getId())) {
          return true;
        }
      }
    }
    return false;
      
  }

  @Override
  public void writeClassFiles(File outputDir) throws IOException {
    RecordClassGenerator generator = new RecordClassGenerator(type, getSuperClass(), fields.values());
    generator.writeClassFile(outputDir);
  }

  private Type getSuperClass() {
    for (FieldStrategy fieldStrategy : fields.values()) {
      if(fieldStrategy instanceof SuperClassFieldStrategy) {
        return ((SuperClassFieldStrategy) fieldStrategy).getType();
      }
    }
    return Type.getType(Object.class);
  }
  
}
