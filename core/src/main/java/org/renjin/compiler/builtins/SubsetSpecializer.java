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
package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;

import java.util.ArrayList;
import java.util.List;

/**
 * Specializes calls to the {@code [} operator
 */
public class SubsetSpecializer implements Specializer, BuiltinSpecializer {


  @Override
  public String getName() {
    return "[";
  }

  @Override
  public String getGroup() {
    return null;
  }

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ArgumentBounds> arguments) {

    ValueBounds source = arguments.get(0).getBounds();
    ValueBounds drop = null;

    List<ValueBounds> subscripts = new ArrayList<>();

    for (int i = 1; i < arguments.size(); i++) {
      ArgumentBounds argument = arguments.get(i);
      if("drop".equals(argument.getName())) {
        drop = argument.getBounds();
      } else {
        subscripts.add(argument.getBounds());
      }
    }

    if (subscripts.size() == 0) {
      return new CompleteSubset(source);
    }

    // If exactly two subscripts are provided
    // Such as x[i,j] or x[i,], AND
    // the source type is known, then treat this as a matrix selection
    SingleRowOrColumn singleRowOrColumn = SingleRowOrColumn.trySpecialize(source, subscripts, drop);
    if(singleRowOrColumn != null) {
      return singleRowOrColumn;
    }

    GetAtomicElement singleElement = GetAtomicElement.trySpecialize(source, subscripts);
    if(singleElement != null) {
      return singleElement;
    }


    return UnspecializedCall.INSTANCE;
    
//    // A single subscript can also contain a matrix in the form
//    //    x1, y1  
//    // [  x2, y2 ]
//    //    x3, y3
//    if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {
//      return new CoordinateMatrixSelection((AtomicVector) subscript);
//    }
//
//    // Otherwise we treat it as an index into a vector  
//    if (subscript instanceof LogicalVector) {
//      return new LogicalSelection((LogicalVector) subscript);
//
//    } else if (subscript instanceof StringVector) {
//      return new NamedSelection((StringVector) subscript);
//
//    } else if (subscript instanceof DoubleVector ||
//        subscript instanceof IntVector) {
//
//      return new VectorIndexSelection((AtomicVector) subscript);
//
//    } else if(subscript == Null.INSTANCE) {
//      return NullSelection.INSTANCE;
//
//    } else {
//      throw new EvalException("invalid subscript type '%s'", subscript.getTypeName());
//    }
//    
//    throw new UnsupportedOperationException();
  }

}
