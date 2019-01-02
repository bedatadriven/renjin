/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Strategy for passing and receiving parameters of primitive type.
 */
public class PrimitiveParamStrategy implements ParamStrategy {
  
  private PrimitiveType type;

  public PrimitiveParamStrategy(PrimitiveType type) {
    this.type = type;
  }

  public PrimitiveParamStrategy(GimplePrimitiveType primitiveType) {
    this(PrimitiveType.of(primitiveType));
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type.jvmType());
  }

  @Override
  public List<String> getParameterNames(String name) {
    return Collections.singletonList(name);
  }

  @Override
  public GExpr emitInitialization(MethodGenerator mv,
                                  GimpleParameter parameter,
                                  List<JLValue> paramVars,
                                  VarAllocator localVars) {
    
    JExpr paramValue = paramVars.get(0);

    if(parameter.isAddressable()) {
      // if this parameter is addressed, then we need to allocate a unit array that can hold the value
      // and be addressed as needed.
      JLValue unitArray = localVars.reserveUnitArray(parameter.getName(), type.jvmType(), Optional.of(paramValue));
      FatPtrPair address = new FatPtrPair(new PrimitiveValueFunction(type), unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return type.fromNonStackValue(value, address);
    } else {
      
      // Otherwise we can just reference the value of the parameter
      return type.fromNonStackValue(paramValue);
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      type.cast(argument.get().toPrimitiveExpr()).jexpr().load(mv);
    } else {
      new ConstantValue(type.jvmType(), 0).load(mv);
    }
  }
}
