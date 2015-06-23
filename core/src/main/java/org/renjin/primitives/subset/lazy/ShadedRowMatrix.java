package org.renjin.primitives.subset.lazy;

import com.google.common.primitives.Ints;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadedRowMatrix extends DoubleVector implements MemoizedComputation {

  private DoubleVector base = null;
  private int columnLength = 1;
  private Map<Integer, DoubleVector> rowMap = new HashMap<Integer, DoubleVector>();
  
  private double[] result;
  
  public ShadedRowMatrix(DoubleVector source)  {
    super(source.getAttributes());
    this.base = source;
    SEXP dimr = base.getAttribute(Symbols.DIM);
    if (!(dimr instanceof IntArrayVector)) {
      throw new RuntimeException("non-integer dimensions? weird!");
    }
    columnLength = ((IntArrayVector)dimr).getElementAsInt(0);
  }
  
  public ShadedRowMatrix withShadedRow(int row, DoubleVector elements) {
    return cloneWithNewAttributes(this.getAttributes()).setShadedRow(row, elements);
  }
  
  public ShadedRowMatrix setShadedRow(int row, DoubleVector elements) {
    rowMap.put(row, elements);
    return this;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  protected ShadedRowMatrix cloneWithNewAttributes(AttributeMap attributes) {    
    ShadedRowMatrix clone = new ShadedRowMatrix(this.base);
    clone.rowMap = new HashMap<Integer, DoubleVector>(rowMap);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / columnLength;
    int row = (index % columnLength)+1;
    if (rowMap.containsKey(row)) {
      return rowMap.get(row).get(col);
    } else {
      return base.get(index);
    }
  }

  @Override
  public int length() {
    return base.length();
  }

  @Override
  public Vector[] getOperands() {
    List<Vector> ops = new ArrayList<Vector>();
    ops.add(base);
    ops.add(new IntArrayVector(Ints.toArray(rowMap.keySet())));
    ops.addAll(rowMap.values());
    return ops.toArray(new Vector[ops.size()]);
  }

  @Override
  public String getComputationName() {
    return "ShadedRowMatrix";
  }

  @Override
  public Vector forceResult() {

    int rowLength = length() / columnLength;
    double matrix[] = base.toDoubleArray();
    for (Map.Entry<Integer, DoubleVector> entry : rowMap.entrySet()) {
      DoubleVector vector = entry.getValue();
      int rowIndex = entry.getKey()-1;
      int index = rowIndex;

      for (int i = 0; i < rowLength; ++i) {
        matrix[index] = vector.getElementAsDouble(i);
        index += columnLength;
      }
    }
    
    this.result = matrix;
    return DoubleArrayVector.unsafe(matrix);
  }

  @Override
  public void setResult(Vector result) {
    this.result = ((DoubleArrayVector) result).toDoubleArrayUnsafe();
  }

  @Override
  public boolean isCalculated() {
    return result != null;
  }
}
