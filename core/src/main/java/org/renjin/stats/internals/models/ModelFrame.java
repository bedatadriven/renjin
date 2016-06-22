package org.renjin.stats.internals.models;

import com.google.common.collect.Lists;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Types;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import java.util.List;

/**
 * Encapsulates a model frame object. 
 * 
 * <p>A model frame is an R object of class "data.frame" that is created by
 * the {@code model.frame} function and contains a column for every unique
 * variable that is referenced by the model formula.
 */
public class ModelFrame {
  
  private final ListVector frame;
  private final int numRows;
  private final List<Variable> variables;
  
  public ModelFrame(ListVector frame) {
    this.frame = frame;
    if(frame.length() == 0) {
      throw new EvalException("do not know how many cases");
    }
    this.numRows = Models.nrows(frame.getElementAsSEXP(0));
    
    variables = Lists.newArrayList();
    for(int i=0; i!=frame.length(); ++i) {
      SEXP vector = frame.getElementAsSEXP(i);
      if(Models.nrows(vector) != numRows) {
        throw new EvalException("variable lengths differ");
      }
      variables.add(createVariable(frame.getName(i), vector));
    }
  }
  
  private Variable createVariable(String name, SEXP vector) {
    if(Types.isFactor(vector)) {
      return new FactorVariable(name, vector);
    } else {
      return new NumericVariable(name, vector);
    }
  }

  public int getNumRows() {
    return numRows;
  }
  
  public Variable getVariable(int index) {
    return variables.get(index);
  }
  
  public Vector getRowNames() {
    return (Vector)frame.getAttribute(Symbols.ROW_NAMES);
  }

  public static int ncols(SEXP s) {
    SEXP t;
    if (s instanceof Vector) {
      Vector dim = (Vector) s.getAttribute(Symbols.DIM);
      if(dim.length() >= 2) {
        return dim.getElementAsInt(1);
      } else {
        return 1;
      }
    } else if (s.inherits("data.frame")) {
      return s.length();
    } else {
      throw new EvalException("object is not a matrix");
    }
  }

  public static int nlevels(SEXP exp) {
    return exp.getAttribute(Symbols.LEVELS).length();
  }
}
