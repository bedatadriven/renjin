package org.renjin.stats.internals.models;

import java.util.Collections;
import java.util.List;

import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

public class NumericVariable extends Variable {

  private final String name;
  private final Vector vector;
  private final int numColumns;
  
  public NumericVariable(String name, SEXP vector) {
    this.name = name;
    this.vector = (Vector)vector;
   
    Vector dim = vector.getAttributes().getDim();
    if(dim.length() < 2) {
      numColumns = 1;
    } else if(dim.length() == 2) {
      numColumns = dim.getElementAsInt(1);
    } else {
      throw new UnsupportedOperationException("variable " + name + " has " + dim.length() + 
          " dimensions, don't know what to do");
    }
  }

  @Override
  public List<? extends ModelMatrixColumn> getModelMatrixColumns() {
    if(numColumns == 1) {
      return Collections.singletonList(new UnivariateColumn(name, vector));
    } else {
      throw new UnsupportedOperationException();
    }
  }

  
}
