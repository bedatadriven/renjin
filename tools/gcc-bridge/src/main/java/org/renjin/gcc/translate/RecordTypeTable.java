package org.renjin.gcc.translate;

import com.google.common.collect.Maps;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.jimple.JimpleClassBuilder;
import org.renjin.gcc.jimple.JimpleFieldBuilder;
import org.renjin.gcc.jimple.JimpleModifiers;
import org.renjin.gcc.jimple.SyntheticJimpleType;
import org.renjin.gcc.translate.type.ImType;
import org.renjin.gcc.translate.type.struct.ImRecordType;
import org.renjin.gcc.translate.type.struct.SimpleRecordType;

import java.util.List;
import java.util.Map;

/**
 * Maintains a mapping of Gimple Record type to our internal
 * {@link org.renjin.gcc.translate.type.struct.ImRecordType} objects.
 * 
 * <p>Struct objects may refer to a Gimple-defined record_type for which we
 * will create a JVM class during compilation, or an existing JVM class to which
 * we will map the fields to existing fields or to getters/setters.
 */
public class RecordTypeTable {

  private final TranslationContext context;

  private final Map<String, ImRecordType> map = Maps.newHashMap();

  private final Map<String, GimpleRecordTypeDef> defs = Maps.newHashMap();
  
  private final Map<String, ImRecordType> providedTypes;

  public RecordTypeTable(List<GimpleCompilationUnit> units, TranslationContext context,
                         Map<String, ImRecordType> providedRecordTypes) {
    this.context = context;
    this.providedTypes = providedRecordTypes;
    for(GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef recordType : unit.getRecordTypes()) {
        defs.put(recordType.getId(), recordType);
      }
    }
  }

  /**
   * Map an external type definition to a named Gimple Type
   * 
   * @param typeName the name of the type, as defined in the C/Fortran code
   * @param type
   */
  public void provideType(String typeName, ImRecordType type) {
    providedTypes.put(typeName, type);
  }

  public ImRecordType resolveStruct(GimpleRecordType recordType) {
    ImRecordType type = map.get(recordType.getId());
    if(type != null) {
      return type;
    } else {
      GimpleRecordTypeDef def = defs.get(recordType.getId());
      if(def == null) {
        throw new RuntimeException("Can't find record type definition for " + recordType);
      }
      
      if(providedTypes.containsKey(def.getName())) {
        ImRecordType providedType = providedTypes.get(def.getName());
        map.put(recordType.getId(), providedType);
        return providedType;

      } else {

        // create a new JVM type to back this array
        JimpleClassBuilder recordClass = context.getJimpleOutput().newClass();
        recordClass.setPackageName(context.getMainClass().getPackageName());
        recordClass.setClassName(context.getMainClass().getClassName() + "$" + def.getName());

        SimpleRecordType struct = new SimpleRecordType(new SyntheticJimpleType(recordClass.getFqcn()));
        map.put(recordType.getId(), struct);

        buildFields(struct, recordClass, def);

        return struct;
      }
    }
  }

  private void buildFields(SimpleRecordType struct, JimpleClassBuilder recordClass, GimpleRecordTypeDef def) {
    for (GimpleField member : def.getFields()) {
      ImType type = context.resolveType(member.getType());
      struct.addMember(member.getName(), type);

      JimpleFieldBuilder field = recordClass.newField();
      field.setName(member.getName());
      field.setType(type.returnType()); // TODO: probably need a fieldType()
      field.setModifiers(JimpleModifiers.PUBLIC);
    }

  }
}
