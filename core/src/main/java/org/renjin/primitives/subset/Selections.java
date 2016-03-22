package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Created by alex on 22-3-16.
 */
public class Selections {

  public static Selection2 parseSelection(List<SEXP> subscripts) {
    if (subscripts.size() == 0) {
      return new CompleteSelection2();
      
    } else if (subscripts.size() == 1) {
      SEXP subscript = subscripts.get(0);
      if (subscript instanceof LogicalVector) {
        return new LogicalSelection((LogicalVector) subscript);

      } else if (subscript instanceof StringVector) {
        return new NamedSelection((StringVector) subscript);

      } else if (subscript instanceof DoubleVector ||
          subscript instanceof IntVector) {

        return new IndexSelection((AtomicVector) subscript);
        
      } else {
        throw new EvalException("invalid subscript type '%s'", subscript.getTypeName());
      }
    } else {
      return new MatrixSelection(subscripts); 
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