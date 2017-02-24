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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.fatptr.FatPtr;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.SimpleTypeStrategy;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;

import java.util.Collections;
import java.util.List;

public class RefPtrParamStrategy<T extends RefPtrExpr> implements ParamStrategy {
  
  private SimpleTypeStrategy<T> typeStrategy;

  public RefPtrParamStrategy(SimpleTypeStrategy<T> typeStrategy) {
    this.typeStrategy = typeStrategy;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Collections.singletonList(typeStrategy.getJvmType());
  }

  @Override
  public List<String> getParameterNames(String name) {
    return Collections.singletonList(name);
  }

  @Override
  public GExpr emitInitialization(MethodGenerator methodVisitor, GimpleParameter parameter, 
                                  List<JLValue> paramVars, VarAllocator localVars) {
    if(parameter.isAddressable()) {
      throw new UnsupportedOperationException("TODO: Addressable parameters");
    }

    return typeStrategy.wrap(paramVars.get(0));
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    if(argument.isPresent()) {
      
      GExpr argumentValue = argument.get();
      if(argumentValue instanceof RefPtrExpr) {
        RefPtrExpr ptrExpr = (RefPtrExpr) argument.get();

        if (ptrExpr.unwrap().getType().equals(typeStrategy.getJvmType())) {
          ptrExpr.unwrap().load(mv);

        } else {
          // Cast null pointers to the appropriate type
          Expressions.cast(ptrExpr.unwrap(), typeStrategy.getJvmType()).load(mv);
        }
      } else if(argumentValue instanceof FatPtr) {
        FatPtr fatPtrExpr = (FatPtr) argumentValue;
        fatPtrExpr.wrap().load(mv);
        
      } else {
        throw new UnsupportedOperationException("argument type: " + argumentValue);
      }
    } else {
      mv.aconst(null);
    }
  }
}
