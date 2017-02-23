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
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;


public class RecordArrayParamStrategy implements ParamStrategy {
  
  private RecordArrayValueFunction valueFunction;
  private final Type arrayType;
  private final int arrayLength;

  public RecordArrayParamStrategy(RecordArrayValueFunction valueFunction, Type arrayType, int arrayLength) {
    this.valueFunction = valueFunction;
    this.arrayType = arrayType;
    this.arrayLength = arrayLength;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(arrayType);
  }

  @Override
  public List<String> getParameterNames(String name) {
    return Collections.singletonList(name);
  }

  @Override
  public RecordArrayExpr emitInitialization(MethodGenerator methodVisitor, 
                                 GimpleParameter parameter, 
                                 List<JLValue> paramVars, 
                                 VarAllocator localVars) {


    return new RecordArrayExpr(valueFunction, paramVars.get(0), arrayLength);
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {

    // We're passing by VALUE, so we have to make a copy of the array.
    if(argument.isPresent()) {
      RecordArrayExpr recordVar = (RecordArrayExpr) argument.get();
      JExpr arrayCopy = recordVar.copyArray();

      arrayCopy.load(mv);
    } else {
      // Argument not supplied, stack will be corrupted
      mv.aconst(null);
    }
  }
}
