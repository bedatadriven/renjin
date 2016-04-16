package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public abstract class RecordTypeStrategy<ExprT extends Expr> implements TypeStrategy<ExprT> {
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

  /**
   * Writes any additional class files required by this strategy
   * @param outputDirectory 
   * @throws IOException
   */
  public abstract void writeClassFiles(File outputDirectory) throws IOException;

  public abstract Expr memberOf(ExprT instance, GimpleFieldRef fieldRef);
}
