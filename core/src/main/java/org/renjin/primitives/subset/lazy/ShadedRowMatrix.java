package org.renjin.primitives.subset.lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import com.google.common.primitives.Ints;

public class ShadedRowMatrix extends DoubleVector implements DeferredComputation {

  private DoubleVector base = null;
  private int colheight = 1;
  private Map<Integer, DoubleVector> fiftyShadesOfDoubles = 
      new HashMap<Integer, DoubleVector>();
  
  public ShadedRowMatrix(DoubleVector source)  {
    super(source.getAttributes());
    this.base = source;
    SEXP dimr = base.getAttribute(Symbols.DIM);
    if (!(dimr instanceof IntArrayVector)) {
      throw new RuntimeException("non-integer dimensions? weird!");
    }
    colheight = ((IntArrayVector)dimr).getElementAsInt(0);
  }
  
  public ShadedRowMatrix withShadedRow(int row, DoubleVector elements) {
    return cloneWithNewAttributes(this.getAttributes()).setShadedRow(row, elements);
  }
  
  public ShadedRowMatrix setShadedRow(int row, DoubleVector elements) {
    fiftyShadesOfDoubles.put(row, elements);
    return this;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  protected ShadedRowMatrix cloneWithNewAttributes(AttributeMap attributes) {    
    ShadedRowMatrix clone = new ShadedRowMatrix(this.base);
    clone.fiftyShadesOfDoubles = new HashMap<Integer, DoubleVector>(fiftyShadesOfDoubles);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / colheight;
    int row = (index % colheight)+1;
    if (fiftyShadesOfDoubles.containsKey(row)) {
      return fiftyShadesOfDoubles.get(row).get(col);
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
    ops.add(new IntArrayVector(Ints.toArray(fiftyShadesOfDoubles.keySet())));
    ops.addAll(fiftyShadesOfDoubles.values());
    return ops.toArray(new Vector[ops.size()]);
  }

  @Override
  public String getComputationName() {
    return "ShadedRowMatrix";
  }

}
