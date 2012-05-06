package org.renjin.primitives.matrix;

import java.util.Collection;

import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;


public interface MatrixBuilder {
  void setRowNames(Vector names);
  void setRowNames(Collection<String> names);
  void setColNames(StringVector names);
  void setColNames(Collection<String> names);
  int getRows();  
  int getCols();
  void setValue(int row, int col, double value);
  void setValue(int row, int col, int value);
  Vector build();
}
