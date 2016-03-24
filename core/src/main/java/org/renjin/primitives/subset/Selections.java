package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Methods for parsing arguments into {@link SelectionStrategy} instances.
 */
class Selections {

  /**
   * Parses a list of {@code [} operator subscript arguments into a {@link SelectionStrategy} instance which 
   * can then be used to select or replace elements from the source.
   * 
   * @param source the source expression
   * @param subscripts the list of subscripts provided as arguments
   */
  public static SelectionStrategy parseSelection(SEXP source, List<SEXP> subscripts) {

    if (subscripts.size() == 0) {
      return new CompleteSelection();
    }

    // If more than one subscript is provided
    // Such as x[i,j] or x[i,j,k], then treat this as a matrix selection
    if (subscripts.size() > 1) {
      return new MatrixSelection(subscripts);
    }
    
    int dimCount = source.getAttributes().getDim().length();
 
    SEXP subscript = subscripts.get(0);

    if(subscript == Symbol.MISSING_ARG) {
      return new CompleteSelection();
    }
    
    // A single subscript can also contain a matrix in the form
    //    x1, y1  
    // [  x2, y2 ]
    //    x3, y3
    if(CoordinateMatrixSelection.isCoordinateMatrix(source, subscript)) {
      return new CoordinateMatrixSelection((AtomicVector) subscript);
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

      return new VectorIndexSelection((AtomicVector) subscript);

    } else if(subscript == Null.INSTANCE) {
      return NullSelection.INSTANCE;

    } else {
      throw new EvalException("invalid subscript type '%s'", subscript.getTypeName());
    }
  }

  /**
   * Parses a list of {@code [[} operator subscript arguments into a {@link SelectionStrategy} instance which 
   * can then be used to select or replace elements from the source.4
   * 
   * <p>Subscripts of the {@code [[} and {@code [[<-} operators are interpretered in subtley 
   * different manners than their single bracket counterparts. For example, logical subscripts are cast
   * to integer indexes, and coordinate matrix subscripts are not recognized.
   * </p>
   */
  public static SelectionStrategy parseSingleSelection(SEXP source, List<SEXP> subscripts) {

    // GNU R throws the error message "invalid subscript type 'symbol'" in this 
    // case, probably becauset the arugments get resolved to Symbol.MISSING, but I think
    // this message is a bit clearer...
    if (subscripts.size() == 0) {
      throw new EvalException("[[ operator requires at least one subscript");
    }

    // If more than one subscript is provided
    // Such as x[i,j] or x[i,j,k], then treat this as a matrix selection
    if (subscripts.size() > 1) {
      return new MatrixSelection(subscripts);
    }

    int dimCount = source.getAttributes().getDim().length();

    SEXP subscript = subscripts.get(0);

    if(subscript == Symbol.MISSING_ARG) {
      throw new EvalException("[[ operator requires at least one subscript");
    }

    // If there is a single subscript, it's interpretation depends on the 
    // shape of the source:
    // - If the source has exactly one dimension, we treat it as a matrix selection
    // - If the source has any other dimensionality, including no explicit dims, treat it as a vector index
    if(dimCount == 1) {
      return new MatrixSelection(subscripts);
    }

    if (subscript instanceof StringVector) {
      return new NamedSelection((StringVector) subscript);

    } else if (
        subscript instanceof DoubleVector ||
        subscript instanceof IntVector ||
        subscript instanceof LogicalVector) {

      return new VectorIndexSelection((AtomicVector) subscript);

    } else if(subscript == Null.INSTANCE) {
      return NullSelection.INSTANCE;

    } else {
      throw new EvalException("invalid subscript type '%s'", subscript.getTypeName());
    }
  }

}