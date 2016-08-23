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
import org.renjin.gcc.codegen.type.voidt.VoidPtrValueFunction;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class RecordClassLayout implements RecordLayout {

  private RecordClassLayoutTree tree;
  private UnionSet unionSet;
  private Type type;

  private TreeMap<Integer, FieldStrategy> fields = new TreeMap<>();

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
        return new AddressableField(type, uniqueFieldName(node), new VoidPtrValueFunction());
      } else {
        return new PrimitivePointerUnionField(type, uniqueFieldName(node));
      }
   
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
      return new PrimitiveUnionField(type, uniqueFieldName(node), commonType.get());
      
    } else {
      Optional<Type> commonType = typeSet.tryComputeCommonType();
      if(commonType.isPresent()) {
        int arrayLength = node.getSize() / GimplePrimitiveType.fromJvmType(commonType.get()).getSize();
        return new RecordArrayField(type, uniqueFieldName(node), commonType.get(), arrayLength);
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

  private boolean isPotentialSuperClass(TypeStrategy strategy) {
    if(strategy instanceof RecordClassTypeStrategy) {
      RecordClassTypeStrategy recordStrategy = (RecordClassTypeStrategy) strategy;
      if (!recordStrategy.getJvmType().equals(this.type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Type getType() {
    return type;
  }


  @Override
  public GExpr memberOf(MethodGenerator mv, RecordValue instance, int offset, int size, TypeStrategy fieldTypeStrategy) {
    
    // If this field is a unioned record type, then return a pointer to ourselves
    if(offset == 0 && fieldTypeStrategy instanceof RecordClassTypeStrategy && 
        ((RecordClassTypeStrategy) fieldTypeStrategy).getJvmType().equals(type)) {
      return instance;
    }

    JExpr instanceRef = Expressions.cast(instance.unwrap(), type);
    JExpr instanceVar = mv.getLocalVarAllocator().tempIfNeeded(mv, instanceRef);

    // Find the logical field that contains this bit range
    Integer fieldStart = fields.floorKey(offset);
    FieldStrategy fieldStrategy = fields.get(fieldStart);

    if(fieldStrategy == null) {
      throw new IllegalStateException(type + " has no field at offset " + offset);
    }
    
    return fieldStrategy.memberExpr(instanceVar, offset - fieldStart, size, fieldTypeStrategy);
  }

  @Override
  public void writeClassFiles(File outputDir) throws IOException {
    RecordClassGenerator generator = new RecordClassGenerator(type, getSuperClass(), fields.values(), 
        unionSet.sizeOf());
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
