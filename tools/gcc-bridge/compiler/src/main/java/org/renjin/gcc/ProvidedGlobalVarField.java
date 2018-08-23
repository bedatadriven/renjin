/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.fun.FunPtrStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.type.record.RecordArrayExpr;
import org.renjin.gcc.codegen.type.record.RecordTypeStrategy;
import org.renjin.gcc.codegen.vptr.VPtrExpr;
import org.renjin.gcc.codegen.vptr.VPtrRecordTypeStrategy;
import org.renjin.gcc.codegen.vptr.VPtrStrategy;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

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

    // The trick here is that we have to find a way to map a JVM field that
    // we compiled earlier, in some other library, to a valid strategy for the
    // requested type.


    TypeStrategy strategy;
    if(typeOracle.getRecordTypes().isMappedToRecordType(field.getType())) {

      strategy = typeOracle.getRecordTypes().getPointerStrategyFor(field.getType());

    } else if(field.getType().isPrimitive()) {
      strategy = new PrimitiveTypeStrategy((GimplePrimitiveType) decl.getType());

    } else if (field.getType().equals(MethodHandle[].class) && decl.getType() instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) decl.getType();
      strategy = new FunPtrStrategy().arrayOf(arrayType);

    } else if (Ptr.class.isAssignableFrom(field.getType())) {
      if (decl.getType() instanceof GimpleRecordType) {
        strategy = new VPtrRecordTypeStrategy(typeOracle.getRecordTypeDef(((GimpleRecordType) decl.getType())));
      } else {
        strategy = new VPtrStrategy(decl.getType().getBaseType());
      }
    } else if(decl.getType() instanceof GimpleRecordType && field.getType().isArray()) {

      // In the translation unit where this global variable was defined, this variable
      // was declared as an array, e.g. byte[4]
      // But now, the translation unit which is referring to this global variable, wants to interpret
      // it as a struct.

      Class<?> componentType = field.getType().getComponentType();
      GimplePrimitiveType primitiveComponentType = GimplePrimitiveType.fromJvmType(Type.getType(componentType));
      FatPtrPair fatPtr = new FatPtrPair(new PrimitiveValueFunction(primitiveComponentType), Expressions.staticField(field));
      VPtrExpr vptr = fatPtr.toVPtrExpr();
      return vptr.valueOf(decl.getType());

    } else {


      strategy = typeOracle.forType(decl.getType());
    }

    boolean readOnly = Modifier.isFinal(field.getModifiers());

    return strategy.providedGlobalVariable(decl, Expressions.staticField(field), readOnly);
  }
}
