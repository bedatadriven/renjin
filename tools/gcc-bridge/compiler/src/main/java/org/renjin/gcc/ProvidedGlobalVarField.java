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
package org.renjin.gcc;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.fun.FunPtrStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.codegen.vptr.VPtrRecordTypeStrategy;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.runtime.Ptr;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ProvidedGlobalVarField implements ProvidedGlobalVar  {
  private Field field;

  public ProvidedGlobalVarField(Field field) {
    this.field = field;
  }

  @Override
  public GExpr createExpr(GimpleVarDecl decl, TypeOracle typeOracle) {

    TypeStrategy strategy;
    if(typeOracle.getRecordTypes().isMappedToRecordType(field.getType())) {
      strategy = typeOracle.getRecordTypes().getPointerStrategyFor(field.getType());

    } else if(field.getType().isPrimitive()) {
      strategy = new PrimitiveTypeStrategy(field.getType());

    } else if (field.getType().equals(MethodHandle[].class) && decl.getType() instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) decl.getType();
      strategy = new FunPtrStrategy().arrayOf(arrayType);

    } else if (Ptr.class.isAssignableFrom(field.getType())) {
      if(decl.getType() instanceof GimpleRecordType) {
        strategy = new VPtrRecordTypeStrategy(typeOracle.getRecordTypeDef(((GimpleRecordType) decl.getType())));
      } else {
        strategy = new VPtrStrategy(decl.getType().getBaseType());
      }
    } else {
      throw new UnsupportedOperationException("Strategy for " + field.getType());
    }

    boolean readOnly = Modifier.isFinal(field.getModifiers());

    return strategy.providedGlobalVariable(decl, Expressions.staticField(field), readOnly);
  }
}
