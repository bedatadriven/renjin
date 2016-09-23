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
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtr;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;


public class RecordClassParamStrategy implements ParamStrategy {
  
  private RecordClassTypeStrategy strategy;

  public RecordClassParamStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }
  
  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(strategy.getJvmType());
  }

  @Override
  public RecordValue emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {
    if(strategy.isUnitPointer()) {
      // If this type can be represented as a unit pointer, then 
      // the address expression is equivalent to the value expression.
      JLValue ref = paramVars.get(0);
      RecordUnitPtr address = new RecordUnitPtr(ref);
      
      return new RecordValue(ref, address);
   
    } else {
      if (parameter.isAddressable()) {
        JLValue array = localVars.reserveUnitArray(parameter.getName(), strategy.getJvmType(),
            Optional.of(Expressions.newObject(strategy.getJvmType())));

        FatPtrPair address = new FatPtrPair(new RecordClassValueFunction(strategy), array);
        RecordValue value = new RecordValue(Expressions.elementAt(array, 0), address);
        
        return value;
        
      } else {
        return new RecordValue(paramVars.get(0));
      }
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      RecordValue recordValue = (RecordValue) argument.get();
      recordValue.getRef().load(mv);
      // We are passing by value, so we need to put a clone of the record on the stack
      mv.invokevirtual(recordValue.getJvmType(), "clone", Type.getMethodDescriptor(recordValue.getJvmType()), false);
      
    } else {
      mv.aconst(null);
    }
  }
}
