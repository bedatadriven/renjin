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
package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.expr.GimpleBitFieldRefExpr;
import org.renjin.gcc.gimple.expr.GimpleComponentRef;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.util.HashMap;
import java.util.Map;

public class RecordFieldUseFinder extends GimpleExprVisitor {

  private final Map<String, GimpleRecordTypeDef> typeDefMap = new HashMap<>();


  public RecordFieldUseFinder(Iterable<GimpleRecordTypeDef> canonicalTypeDefs) {
    for (GimpleRecordTypeDef canonicalTypeDef : canonicalTypeDefs) {
      typeDefMap.put(canonicalTypeDef.getId(), canonicalTypeDef);
    }
  }

  @Override
  public void visitComponentRef(GimpleComponentRef componentRef) {
    super.visitComponentRef(componentRef);

    GimpleFieldRef fieldRef = componentRef.getMember();
    GimpleRecordType recordType = (GimpleRecordType) componentRef.getValue().getType();
    GimpleRecordTypeDef recordTypeDef = findRecordTypeDef(recordType);

    markReferencedFields(fieldRef, recordTypeDef);
  }

  @Override
  public void visitConstructor(GimpleConstructor constructor) {
    super.visitConstructor(constructor);

    if(constructor.getType() instanceof GimpleRecordType) {
      GimpleRecordType recordType = (GimpleRecordType) constructor.getType();
      GimpleRecordTypeDef recordTypeDef = findRecordTypeDef(recordType);

      for (GimpleConstructor.Element element : constructor.getElements()) {
        GimpleFieldRef fieldRef = (GimpleFieldRef) element.getField();
        markReferencedFields(fieldRef, recordTypeDef);
      }
    }
  }


  private void markReferencedFields(GimpleFieldRef fieldRef, GimpleRecordTypeDef recordTypeDef) {
    // Mark fields that are referenced.
    for (GimpleField field : recordTypeDef.getFields()) {
      if(field.getOffset() == fieldRef.getOffset() && field.getType().equals(fieldRef.getType())) {
        field.setReferenced(true);
      }
    }
  }


  @Override
  public void visitBitFieldRef(GimpleBitFieldRefExpr bitFieldRef) {
    super.visitBitFieldRef(bitFieldRef);

    GimpleRecordType recordType = (GimpleRecordType) bitFieldRef.getValue().getType();
    GimpleRecordTypeDef recordTypeDef = findRecordTypeDef(recordType);

    int bitRangeStart = bitFieldRef.getOffset();
    int bitRangeEnd = bitFieldRef.getOffset() + bitFieldRef.getSize();

    for (GimpleField field : recordTypeDef.getFields()) {
      int fieldStart = field.getOffset();
      int fieldEnd = fieldStart + field.getSize();

      boolean disjoint = (bitRangeStart >= fieldEnd) || (bitRangeEnd <= fieldStart);

      if(!disjoint) {
        field.setReferenced(true);
      }
    }
  }

  private GimpleRecordTypeDef findRecordTypeDef(GimpleRecordType recordType) {
    GimpleRecordTypeDef recordTypeDef = typeDefMap.get(recordType.getId());

    if(recordTypeDef == null) {
      throw new IllegalStateException("No type def for id " + recordTypeDef.getId());
    }
    return recordTypeDef;
  }
}
