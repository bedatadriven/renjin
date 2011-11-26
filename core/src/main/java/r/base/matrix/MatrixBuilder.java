package r.base.matrix;

import java.util.Collection;

import r.lang.StringVector;
import r.lang.Vector;

public interface MatrixBuilder {
  void setRowNames(StringVector names);
  void setRowNames(Collection<String> names);
  void setColNames(StringVector names);
  void setColNames(Collection<String> names);
  int getRows();  
  int getCols();
  void setValue(int row, int col, double value);
  void setValue(int row, int col, int value);
  Vector build();
}
