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
package org.renjin.compiler.builtins;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Specialization for builtins that are marked {@link org.renjin.invoke.annotations.DataParallel} and
 * whose arguments are "recycled" for multiple calls.
 */
public class DataParallelCall implements Specialization {

  private final String name;
  private final JvmMethod method;
  private List<ValueBounds> argumentTypes;
  private final ValueBounds valueBounds;
  private final Type type;

  public DataParallelCall(Primitives.Entry primitive, JvmMethod method, List<ValueBounds> argumentTypes) {
    this.name = primitive.name;
    this.method = method;
    this.argumentTypes = argumentTypes;
    this.valueBounds = computeBounds(argumentTypes);
    this.type = valueBounds.storageType();
  }

  
  private ValueBounds computeBounds(List<ValueBounds> argumentBounds) {
    
    List<ValueBounds> recycledArguments = recycledArgumentBounds(argumentBounds);

    int resultLength = computeResultLength(argumentTypes);

    ValueBounds.Builder bounds = new ValueBounds.Builder();
    bounds.setType(method.getReturnType());
    bounds.setLength(resultLength);
    
    switch (method.getPreserveAttributesStyle()) {
      case NONE:
        bounds.setEmptyAttributes();
        break;
      case STRUCTURAL:
        buildStructuralBounds(bounds, argumentBounds, resultLength);
        break;
      case ALL:
        buildAllBounds(bounds, argumentBounds, resultLength);
        break;
    }
    
    return bounds.build();
  }



  /**
   * Makes a list of {@link ValueBounds} for @Recycled arguments.
   */
  private List<ValueBounds> recycledArgumentBounds(List<ValueBounds> argumentBounds) {
    List<ValueBounds> list = Lists.newArrayList();
    Iterator<ValueBounds> argumentIt = argumentBounds.iterator();
    for (JvmMethod.Argument formal : method.getFormals()) {
      if (formal.isRecycle()) {
        list.add(argumentIt.next());
      }
    }
    return list;
  }
  
  private int computeResultLength(List<ValueBounds> argumentBounds) {
    Iterator<ValueBounds> it = argumentBounds.iterator();
    int resultLength = 0;
    
    while(it.hasNext()) {
      int argumentLength = it.next().getLength();
      if(argumentLength == ValueBounds.UNKNOWN_LENGTH) {
        return ValueBounds.UNKNOWN_LENGTH;
      }
      if(argumentLength == 0) {
        return 0;
      }
      resultLength = Math.max(resultLength, argumentLength);
    }

    return resultLength;
  }
  
  private void buildStructuralBounds(ValueBounds.Builder bounds, List<ValueBounds> argumentBounds, int resultLength) {

    Map<Symbol, SEXP> attributes = new HashMap<>();
    attributes.put(Symbols.DIM, combineAttribute(Symbols.DIM, argumentBounds, resultLength));
    attributes.put(Symbols.DIMNAMES, combineAttribute(Symbols.DIM, argumentBounds, resultLength));
    attributes.put(Symbols.NAMES, combineAttribute(Symbols.DIM, argumentBounds, resultLength));
    bounds.setClosedAttributes(attributes);
    
  }
  
  private SEXP combineAttribute(Symbol symbol, List<ValueBounds> argumentBounds, int resultLength) {

    // If we don't know the result length, we don't know which 
    // argument to take the attributes from.
    if(resultLength == ValueBounds.UNKNOWN_LENGTH && argumentBounds.size() > 1) {
      return null; // unknown
    }
    
    for (ValueBounds argumentBound : argumentBounds) {
      if (argumentBound.getLength() == resultLength) {

        SEXP value = argumentBound.getAttributeIfConstant(symbol);
        if (value != Null.INSTANCE) {
          return value;
        }
      }
    }
    return Null.INSTANCE;
  }


  private void buildAllBounds(ValueBounds.Builder bounds, List<ValueBounds> argumentBounds, int resultLength) {


    // If we don't know the result length, we don't know which 
    // argument to take the attributes from.
    if(resultLength == ValueBounds.UNKNOWN_LENGTH && argumentBounds.size() > 1) {
      // TOOD: if all argument bounds have closed attribute sets, then we can still 
      // infer SOME information
      return;
    } 

    Map<Symbol, SEXP> attributes = new HashMap<>();

    boolean open = false;
    
    for (ValueBounds argumentBound : argumentBounds) {
      if (argumentBound.getLength() == resultLength) {
        
        if(argumentBound.isAttributeSetOpen()) {
          open = true;
        }

        for (Map.Entry<Symbol, SEXP> entry : argumentBound.getAttributeBounds().entrySet()) {
          if(!attributes.containsKey(entry.getKey())) {
            attributes.put(entry.getKey(), entry.getValue());
          }
        }
      }
    }
    bounds.setAttributeBounds(attributes);
    bounds.setAttributeSetOpen(open);
  }



  public Specialization specializeFurther() {
    if(valueBounds.getLength() == 1) {
      DoubleBinaryOp op = DoubleBinaryOp.trySpecialize(name, method, valueBounds);
      if(op != null) {
        return op;
      }
      return new DataParallelScalarCall(method, argumentTypes, valueBounds).trySpecializeFurther();
    }
    return this;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void load(EmitContext emitContext, InstructionAdapter mv, List<IRArgument> arguments) {
    throw new UnsupportedOperationException();
  }
}
