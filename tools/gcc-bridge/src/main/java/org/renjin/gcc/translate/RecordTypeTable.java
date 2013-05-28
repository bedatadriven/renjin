package org.renjin.gcc.translate;

import com.google.common.collect.Maps;

import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.type.struct.ImRecordType;
import org.renjin.gcc.translate.type.struct.SimpleRecordType;

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

  public RecordTypeTable(TranslationContext context) {
    this.context = context;
  }

  public ImRecordType resolveStruct(GimpleRecordType recordType) {
    if (map.containsKey(recordType.getName())) {
      return map.get(recordType.getName());
    } else {
      SimpleRecordType struct = new SimpleRecordType(context, recordType);
      map.put(recordType.getName(), struct);
      return struct;
    }
  }
}
