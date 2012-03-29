package org.renjin.primitives.models;

import org.renjin.eval.EvalException;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class ModelVariable {

  private Vector vector;
 
  private int numLevels;
  private boolean ordered;
  private int columns;
   
  public ModelVariable(SEXP exp) {
    if(!(exp instanceof Vector)) {
      throw new EvalException("Invalid model variable");
    }
    this.vector = (Vector)exp;

    /* This section of the code checks the types of the variables
       in the model frame.  Note that it should really only check
       the variables if they appear in a term in the model.
       Because it does not, we need to allow other types here, as they
       might well occur on the LHS.
       The R code converts all character variables in the model frame to
       factors, so the only types that ought to be here are logical,
       integer (including factor), numeric and complex.
     */
    
    
  }
}
