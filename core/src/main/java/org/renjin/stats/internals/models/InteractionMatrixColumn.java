package org.renjin.stats.internals.models;

import java.util.List;

/**
 * Column containing the interaction between two or more 
 * variables.
 *
 */
public class InteractionMatrixColumn implements ModelMatrixColumn {

  private final ModelMatrixColumn[] variables;
  private final String name;
  
  public InteractionMatrixColumn(ModelMatrixColumn[] variables) {
    super();
    this.variables = variables;

    // compose the name of this column in the
    // form x:y:z
    StringBuilder name = new StringBuilder();
    for(ModelMatrixColumn variable : variables) {
      if(name.length() > 0) {
        name.append(":");
      }
      name.append(variable.getName());
    }
    this.name = name.toString();
  }
  
  public InteractionMatrixColumn(List<ModelMatrixColumn> parts) {
    this(parts.toArray(new ModelMatrixColumn[parts.size()]));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public double getValue(int observationIndex) {
    double value = 1;
    for(int i=0;i!=variables.length;++i) {
      value *= variables[i].getValue(observationIndex);
    }
    return value;
  }
}
