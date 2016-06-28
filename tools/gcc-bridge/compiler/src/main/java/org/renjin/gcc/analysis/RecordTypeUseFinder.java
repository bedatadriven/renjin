package org.renjin.gcc.analysis;

import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enumerate Record types actually used by compiled code
 */
public class RecordTypeUseFinder extends RecordTypeUseVisitor {
  
  private final Map<String, GimpleRecordTypeDef> typeDefMap = new HashMap<>();
  
  private final Set<String> usedTypeDefs = Sets.newHashSet();

  public RecordTypeUseFinder(Iterable<GimpleRecordTypeDef> canonicalTypeDefs) {
    for (GimpleRecordTypeDef canonicalTypeDef : canonicalTypeDefs) {
      typeDefMap.put(canonicalTypeDef.getId(), canonicalTypeDef);
    }
  }

  @Override
  protected void visitRecordType(GimpleRecordType type) {
    GimpleRecordTypeDef typeDef = typeDefMap.get(type.getId());
    
    if(typeDef == null) {
      throw new IllegalStateException();
    }
    
    if(usedTypeDefs.add(typeDef.getId())) {
      visit(typeDef);
    }
  }

  public boolean isUsed(GimpleRecordTypeDef typeDef) {
    return usedTypeDefs.contains(typeDef.getId());
  }
}
