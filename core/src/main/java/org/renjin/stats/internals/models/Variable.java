package org.renjin.stats.internals.models;

import java.util.List;

public abstract class Variable {


  public abstract List<? extends ModelMatrixColumn> getModelMatrixColumns();
}