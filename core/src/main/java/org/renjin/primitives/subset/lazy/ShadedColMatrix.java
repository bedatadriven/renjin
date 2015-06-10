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

public class ShadedColMatrix extends DoubleVector implements DeferredComputation {

  private DoubleVector base = null;
  private int colheight = 1;
  private Map<Integer, DoubleVector> fiftyShadesOfDoubles = 
      new HashMap<Integer, DoubleVector>();
  
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
    fiftyShadesOfDoubles.put(col, elements);
    return this;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  protected ShadedColMatrix cloneWithNewAttributes(AttributeMap attributes) {    
    ShadedColMatrix clone = new ShadedColMatrix(this.base);
    clone.fiftyShadesOfDoubles = new HashMap<Integer, DoubleVector>(fiftyShadesOfDoubles);
    return clone;
  }

  @Override
  public double getElementAsDouble(int index) {
    int col = index / colheight + 1;
    int row = (index % colheight);
    if (fiftyShadesOfDoubles.containsKey(col)) {
      return fiftyShadesOfDoubles.get(col).get(row);
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
    return "ShadedColMatrix";
  }

}
