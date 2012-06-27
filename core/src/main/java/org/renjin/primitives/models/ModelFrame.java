package org.renjin.primitives.models;

import com.google.common.collect.Lists;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Types;
import org.renjin.sexp.*;

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
    this.numRows = frame.getElementAsSEXP(0).length();
    
    variables = Lists.newArrayList();
    for(int i=0; i!=frame.length(); ++i) {
      variables.add(new Variable(
          frame.getName(i),
          frame.getElementAsSEXP(i)));
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
  
  public class Variable {
  
    private String name;
    private Vector vector;
    private boolean ordered;
    private int numLevels;
    private int numColumns;
    
    public Variable(String name, SEXP vector) {
      this.name = name;
      if(vector.length() != numRows) {
        throw new EvalException("variable lengths differ");
      }
      this.vector = (Vector)vector;
      this.ordered = isOrderedFactor(vector);
      numColumns = ncols(vector);

      if(Types.isFactor(vector)) {
        numLevels = vector.getAttribute(Symbols.LEVELS).length();
        if(numLevels < 1) {
          throw new EvalException("variable has no levels");
        }
        
      } else if(vector instanceof LogicalVector) {
        numLevels = 2;
        
      } else if(Types.isNumeric(vector)) {
        numLevels = 0;
        vector = Types.asVector((Vector)vector, "double");
      } 
    }

    public int getNumLevels() {
      return numLevels;
    }

    public int getNumColumns() {
      return numColumns;
    }
    
    public boolean isFactor() {
      return numLevels > 0;
    }

    public boolean isLogical() {
      return vector instanceof LogicalVector;
    }

    public String getName() {
      return name;
    }

    public boolean isComplex() {
      return false;
    }

    public Vector getVector() {
      return vector;
    }

    public boolean isNumeric() {
      return Types.isNumeric(vector);
    }
  }
  
  public static int ncols(SEXP s)
  {
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

  public static boolean isOrderedFactor(SEXP vector) {
    return vector instanceof IntVector &&
        vector.inherits("factor") && 
        vector.inherits("ordered");
  }
  
  public static boolean isUnorderedFactor(SEXP vector) {
    return vector instanceof IntVector &&
        vector.inherits("factor") &&
        !vector.inherits("ordered");
  }
  
  public static int nlevels(SEXP exp) {
    return exp.getAttribute(Symbols.LEVELS).length();
  }
}
