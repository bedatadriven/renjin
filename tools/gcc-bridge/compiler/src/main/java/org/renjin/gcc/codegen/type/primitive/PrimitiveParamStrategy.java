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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for passing and receiving parameters of primitive type.
 */
public class PrimitiveParamStrategy implements ParamStrategy {
  
  private Type type;

  public PrimitiveParamStrategy(Type type) {
    this.type = type;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(type);
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
      JLValue unitArray = localVars.reserveUnitArray(parameter.getName(), type, Optional.of(paramValue));
      FatPtrPair address = new FatPtrPair(new PrimitiveValueFunction(type), unitArray);
      JExpr value = Expressions.elementAt(address.getArray(), 0);
      return new PrimitiveValue(value, address);
    } else {
      
      // Otherwise we can just reference the value of the parameter
      return new PrimitiveValue(paramValue);
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      ((PrimitiveValue) argument.get()).getExpr().load(mv);
    } else {
      new ConstantValue(type, 0).load(mv);
    }
  }
}
