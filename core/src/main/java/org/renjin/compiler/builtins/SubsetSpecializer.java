package org.renjin.compiler.builtins;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.RuntimeState;
import org.renjin.sexp.Symbol;

import java.util.List;

/**
 * Specializes calls to the {@code [} operator
 */
public class SubsetSpecializer implements Specializer {

  @Override
  public Specialization trySpecialize(RuntimeState runtimeState, List<ValueBounds> argumentTypes) {

    ValueBounds source = argumentTypes.get(0);
    List<ValueBounds> subscripts = argumentTypes.subList(1, argumentTypes.size());
    
    if (subscripts.size() == 0) {
      return new CompleteSubset(source);
    }

    // If more than one subscript is provided
    // Such as x[i,j] or x[i,j,k], then treat this as a matrix selection
    if (subscripts.size() > 1) {
      return new MatrixSubset(source, subscripts).tryFurtherSpecialize();
    }

    ValueBounds subscript = subscripts.get(0);

    if(subscript.isConstant() && subscript.getConstantValue() == Symbol.MISSING_ARG) {
      return new CompleteSubset(source);
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
