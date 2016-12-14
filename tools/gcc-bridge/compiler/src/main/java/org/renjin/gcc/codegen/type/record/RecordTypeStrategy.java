/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeStrategy;
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

  public abstract GExpr memberOf(MethodGenerator mv, ExprT instance, int offset, int size, TypeStrategy fieldTypeStrategy);

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
