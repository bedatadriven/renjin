package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public abstract class RecordTypeStrategy extends TypeStrategy {
  protected final GimpleRecordTypeDef recordTypeDef;
  protected final GimpleRecordType recordType;

  public RecordTypeStrategy(GimpleRecordTypeDef recordTypeDef) {
    this.recordType = new GimpleRecordType(recordTypeDef);
    this.recordTypeDef = recordTypeDef;
  }

  public GimpleRecordType getRecordType() {
    return recordType;
  }

  public GimpleRecordTypeDef getRecordTypeDef() {
    return recordTypeDef;
  }

  public abstract void linkFields(TypeOracle typeOracle);

  public abstract void writeClassFiles(File outputDirectory) throws IOException;
}
