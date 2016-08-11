package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

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

  public abstract GExpr memberOf(MethodGenerator mv, ExprT instance, GimpleFieldRef fieldRef, TypeStrategy fieldTypeStrategy);

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
