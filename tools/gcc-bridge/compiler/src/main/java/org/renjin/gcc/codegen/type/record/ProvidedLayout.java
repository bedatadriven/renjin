package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Layout for a RecordClass based on an existing JVM class.
 */
public class ProvidedLayout implements RecordLayout {

  private GimpleRecordTypeDef typeDef;
  private Type type;

  private Map<Integer, FieldStrategy> fieldMap = new HashMap<>();
  
  public ProvidedLayout(GimpleRecordTypeDef typeDef, Type type) {
    this.typeDef = typeDef;
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    for (GimpleField gimpleField : typeDef.getFields()) {
      FieldStrategy fieldStrategy = typeOracle.forField(type, gimpleField);
      fieldMap.put(gimpleField.getOffset(), fieldStrategy);
    }
  }

  @Override
  public void writeClassFiles(File outputDir) throws IOException {
    
  }

  @Override
  public GExpr memberOf(MethodGenerator mv, RecordValue instance, GimpleFieldRef fieldRef, TypeStrategy fieldTypeStrategy) {
    FieldStrategy fieldStrategy = fieldMap.get(fieldRef.getOffset());
    if(fieldStrategy == null) {
      throw new IllegalStateException("Cannot find field " + fieldRef);
    }
    return fieldStrategy.memberExpr(instance.unwrap(), 0, null);
  }
}
