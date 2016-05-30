package org.renjin.stats.internals.models;

import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

import java.util.List;


public class FactorVariable extends Variable {

  private final String name;
  private final Vector vector;
  private ContrastMatrix contrastMatrix;
  
  public FactorVariable(String name, SEXP vector) {
    this.name = name;
    this.vector = (Vector)vector;
    
    SEXP contrasts = vector.getAttribute(Symbol.get("contrasts"));
    if(contrasts != Null.INSTANCE) {
      contrastMatrix = new ContrastMatrix(contrasts);
    } else {
      throw new EvalException("Invalid contrast matrix for " + name);
    }
  }

  @Override
  public List<ModelMatrixColumn> getModelMatrixColumns() {
    List<ModelMatrixColumn> columns = Lists.newArrayList();
    for(int i=0;i!=contrastMatrix.getNumDummyVariables();++i) {
      columns.add(new DummyColumn(
          name + contrastMatrix.getDummyVariableName(i), 
          vector, contrastMatrix, i));
    }
    return columns;
  }
}
