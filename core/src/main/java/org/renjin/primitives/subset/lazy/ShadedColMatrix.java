package org.renjin.primitives.subset.lazy;

import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShadedColMatrix extends DoubleVector implements MemoizedComputation {

  private DoubleVector base = null;
  private int colheight = 1;
  private Map<Integer, DoubleVector> columnMap = new HashMap<Integer, DoubleVector>();
  
  private double result[];
  
  public ShadedColMatrix(DoubleVector source)  {
    super(source.getAttributes());
    this.base = source;
    SEXP dimr = base.getAttribute(Symbols.DIM);
    if (!(dimr instanceof IntArrayVector)) {
      throw new RuntimeException("non-integer dimensions? weird!");
    }
    colheight = ((IntArrayVector)dimr).getElementAsInt(0);
  }
  
  public ShadedColMatrix withShadedCol(int col, DoubleVector elements) {
    return cloneWithNewAttributes(this.getAttributes()).setShadedCol(col, elements);
  }
  
  public ShadedColMatrix setShadedCol(int col, DoubleVector elements) {
    columnMap.put(col, elements);
    return this;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  protected ShadedColMatrix cloneWithNewAttributes(AttributeMap attributes) {    
    ShadedColMatrix clone = new ShadedColMatrix(this.base);
    clone.columnMap = new HashMap<Integer, DoubleVector>(columnMap);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / colheight + 1;
    int row = (index % colheight);
    if (columnMap.containsKey(col)) {
      return columnMap.get(col).get(row);
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
    ops.add(new IntArrayVector(Ints.toArray(columnMap.keySet())));
    ops.addAll(columnMap.values());
    return ops.toArray(new Vector[ops.size()]);
  }

  @Override
  public String getComputationName() {
    return "ShadedColMatrix";
  }

  @Override
  public Vector forceResult() {
    int numColumns = length() / colheight;
    // allocate a copy of the base array
    double [] matrix = base.toDoubleArray();
    int index = 0;
    for(int col=0;col<numColumns;++col) {
      DoubleVector column = columnMap.get(col+1);
      if(column == null) {
        // skip this column, use the base values
        index += colheight;
      
      } else {
        // read this column from the operand
        for(int row=0;row<colheight;++row) {
          matrix[index++] = column.getElementAsDouble(row);
        }
      }
    }
    this.result = matrix;
    
    return DoubleArrayVector.unsafe(this.result);
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
