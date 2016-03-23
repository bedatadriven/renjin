package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.List;

public class Selections {

  public static Selection2 parseSelection(SEXP source, List<SEXP> subscripts) {

    if (subscripts.size() == 0) {
      return new CompleteSelection2();
    }

    // If more than one subscript is provided
    // Such as x[i,j] or x[i,j,k], then treat this as a matrix selection
    if (subscripts.size() > 1) {
      return new MatrixSelection(subscripts);
    }

    
    int dimCount = source.getAttributes().getDim().length();
 
    SEXP subscript = subscripts.get(0);

    if(subscript == Symbol.MISSING_ARG) {
      return new CompleteSelection2();
    }

    // If there is a single subscript, it's interpretation depends on the 
    // shape of the source:
    // - If the source has exactly one dimension, we treat it as a matrix selection
    // - If the source has any other dimensionality, including no explicit dims, treat it as a vector index
    if(dimCount == 1) {
      return new MatrixSelection(subscripts);
    } 
    
    if (subscript instanceof LogicalVector) {
      return new LogicalSelection((LogicalVector) subscript);

    } else if (subscript instanceof StringVector) {
      return new NamedSelection((StringVector) subscript);

    } else if (subscript instanceof DoubleVector ||
        subscript instanceof IntVector) {

      return new IndexSelection((AtomicVector) subscript);

    } else if(subscript == Null.INSTANCE) {
      return NullSelection.INSTANCE;

    } else {
      throw new EvalException("invalid subscript type '%s'", subscript.getTypeName());
    }
  }


  public static void checkUnitLength(SEXP sexp) {
    if(sexp.length() < 1) {
      throw new EvalException("attempt to select less than one element.");
    }
    if(sexp.length() > 1) {
      throw new EvalException("attempt to select mroe than one element.");
    }
  }
}