package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.UnsupportedCastException;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public abstract class RecordTypeStrategy<ExprT extends GExpr> implements TypeStrategy<ExprT> {
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

  public abstract GExpr memberOf(ExprT instance, GimpleFieldRef fieldRef);

  @Override
  public ExprT cast(GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    throw new UnsupportedCastException();
  }


  public static boolean isCircularField(GimpleRecordTypeDef typeDef, GimpleField gimpleField) {
    // GCC emits this weird member at the end of class 
    // need to figure out why this is there 
    if(gimpleField.getType() instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) gimpleField.getType();
      if(recordType.getId().equals(typeDef.getId())) {
        return true;
      }
    }
    return false;
  }

}
