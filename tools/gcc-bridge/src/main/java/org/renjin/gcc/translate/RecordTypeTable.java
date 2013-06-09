package org.renjin.gcc.translate;

import com.google.common.collect.Maps;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.translate.TranslationContext;
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

  private final Map<Integer, ImRecordType> map = Maps.newHashMap();

  private final Map<Integer, GimpleRecordTypeDef> defs = Maps.newHashMap();

  public RecordTypeTable(List<GimpleCompilationUnit> units, TranslationContext context) {
    this.context = context;
    for(GimpleCompilationUnit unit : units) {
      for (GimpleRecordTypeDef recordType : unit.getRecordTypes()) {
        defs.put(recordType.getId(), recordType);
      }
    }
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

      SimpleRecordType struct = new SimpleRecordType(context, def);
      map.put(recordType.getId(), struct);
      struct.resolveFields();
      return struct;
    }
  }
}
