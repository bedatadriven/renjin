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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.runtime.ObjectPtr;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for using FatPtrs as a single wrapped fat pointer
 */
public class WrappedFatPtrParamStrategy implements ParamStrategy {

  private ValueFunction valueFunction;

  public WrappedFatPtrParamStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  @Override
  public List<Type> getParameterTypes() {
    return Lists.newArrayList(Wrappers.wrapperType(valueFunction.getValueType()));
  }

  @Override
  public List<String> getParameterNames(String name) {
    return Collections.singletonList(name);
  }

  @Override
  public GExpr emitInitialization(MethodGenerator mv, GimpleParameter parameter, List<JLValue> paramVars, VarAllocator localVars) {

    JLValue wrapper = paramVars.get(0);

    if(parameter.isAddressable()) {
      
      // Allocate a unit array for this parameter
      JLValue unitArray = localVars.reserveUnitArray(parameter.getName() + "$address",
          wrapper.getType(), Optional.<JExpr>of(wrapper));

      return new DereferencedFatPtr(unitArray, Expressions.constantInt(0), 
          new FatPtrValueFunction(valueFunction)).valueOf();

    } else if(valueFunction.getValueType().getSort() == Type.OBJECT) {
      return new WrappedFatPtrExpr(valueFunction, wrapper);

    } else {
      
      // For pointers to primitive, unpack for efficiency
      
      JLValue array = localVars.reserve(parameter.getName() + "$array", Wrappers.valueArrayType(valueFunction.getValueType()));
      JLValue offset = localVars.reserveInt(parameter.getName() + "$offset");

      JExpr arrayField = Wrappers.arrayField(wrapper, valueFunction.getValueType());
      JExpr offsetField = Wrappers.offsetField(wrapper);

      array.store(mv, arrayField);
      offset.store(mv, offsetField);

      return new FatPtrPair(valueFunction, array, offset);
    }
  }

  @Override
  public void loadParameter(MethodGenerator mv, Optional<GExpr> argument) {
    
    if(!argument.isPresent()) {
      mv.aconst(null);
      return;
    }
    
    GExpr argumentValue = argument.get();
    Type wrappedType = Wrappers.wrapperType(valueFunction.getValueType());

    // Check for a void*
    if(argumentValue instanceof VoidPtr) {
      VoidPtr voidPtr = (VoidPtr) argumentValue;
      voidPtr.unwrap().load(mv);
      if(wrappedType.equals(Type.getType(ObjectPtr.class))) {
        // Need to provide type
        mv.visitLdcInsn(valueFunction.getValueType());
        mv.invokestatic(wrappedType, "cast", Type.getMethodDescriptor(wrappedType,
            Type.getType(Object.class), Type.getType(Class.class)));

      } else {
        mv.invokestatic(wrappedType, "cast", Type.getMethodDescriptor(wrappedType, Type.getType(Object.class)));
      }
    } else if(argumentValue instanceof RefPtrExpr) {
      RefPtrExpr refPtr = (RefPtrExpr) argumentValue;
      JExpr wrappedPtr = Expressions.cast(refPtr.unwrap(), wrappedType);
      wrappedPtr.load(mv);
    
    } else if(argumentValue instanceof FatPtr) {
      FatPtr fatPtrExpr = (FatPtr) argumentValue;
      JExpr wrappedExpr = Expressions.cast(fatPtrExpr.wrap(), wrappedType);
      wrappedExpr.load(mv);


    } else {
      throw new IllegalArgumentException("argument: " + argumentValue);
    }
  }
}
