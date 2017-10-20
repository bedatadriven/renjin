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
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Layout for a RecordClass based on an existing JVM class.
 */
public class ProvidedLayout implements RecordLayout {

  private GimpleRecordTypeDef typeDef;
  private Type type;

  private Map<Integer, FieldStrategy> fieldMap = new HashMap<>();
  
  public ProvidedLayout(GimpleRecordTypeDef typeDef, Type type) {
    this.typeDef = typeDef;
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void linkFields(TypeOracle typeOracle) {
    for (GimpleField gimpleField : typeDef.getFields()) {
      FieldStrategy fieldStrategy = typeOracle.forField(type, gimpleField);
      fieldMap.put(gimpleField.getOffset(), fieldStrategy);
    }
  }

  @Override
  public void writeClassFiles(File outputDir) throws IOException {

  }

  @Override
  public GExpr memberOf(MethodGenerator mv, RecordValue instance, int offset, int size, GimpleType type) {
    FieldStrategy fieldStrategy = fieldMap.get(offset);
    if(fieldStrategy == null) {
      throw new IllegalStateException("Cannot find field at offset " + offset);
    }
    return fieldStrategy.memberExpr(mv, instance.unwrap(), 0, size, type);
  }

  @Override
  public RecordValue clone(MethodGenerator mv, RecordValue recordValue) {
    return recordValue.doClone(mv);
  }
}
