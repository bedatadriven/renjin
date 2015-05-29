package org.renjin.primitives.matrix;

import org.renjin.sexp.Vector;

import java.util.Collection;


public interface MatrixBuilder {
  void setRowNames(Vector names);
  void setRowNames(Collection<String> names);
  void setColNames(Vector names);
  void setColNames(Collection<String> names);
  int getRows();  
  int getCols();
  void setValue(int row, int col, double value);
  void setValue(int row, int col, int value);
  Vector build();
}
