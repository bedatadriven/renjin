package org.renjin.gcc.codegen.type.record;

import com.google.common.collect.Iterables;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.util.List;

/**
 * One or more {@link GimpleRecordTypeDef}s that need to have compatible memory layout.
 */
public class UnionSet {
  private final List<GimpleRecordTypeDef> unions;
  private final List<GimpleRecordTypeDef> records;
  
  private final FieldTypeSet typeSet;

  public UnionSet(List<GimpleRecordTypeDef> unions, List<GimpleRecordTypeDef> records) {
    this.unions = unions;
    this.records = records;
    this.typeSet = new FieldTypeSet(unions, records);
  }

  public List<GimpleRecordTypeDef> getUnions() {
    return unions;
  }

  public List<GimpleRecordTypeDef> getRecords() {
    return records;
  }
  
  public int getSize() {
    return records.size();
  }

  public boolean isSingleton() {
    return records.size() == 1;
  }

  public GimpleRecordTypeDef singleton() {
    return Iterables.getOnlyElement(records);
  }

  public FieldTypeSet getTypeSet() {
    return typeSet;
  }

  public Iterable<GimpleRecordTypeDef> getAllTypes() {
    return Iterables.concat(unions, records);
  }
}
