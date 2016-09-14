package org.renjin.primitives.matrix;

import org.renjin.primitives.vector.AttributeDecoratingVector;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class DeferredCrossprod extends DoubleVector implements
    MemoizedComputation {

  private final AtomicVector x;
  private final AtomicVector y;
  private double[] crossproduct = null;

  private static AttributeMap calcDim(AtomicVector x, AtomicVector y, AttributeMap attributes) {
    if (y == Null.INSTANCE) {
      y = x;
    }
    AttributeMap.Builder ab = AttributeMap.builder();
    ab.addAllFrom(attributes);
    ab.setDim(
        x.getAttributes().getDimArray()[1], 
        y.getAttributes().getDimArray()[1]);
    
    return ab.build();
  }

  public DeferredCrossprod(AtomicVector x, AtomicVector y,
      AttributeMap attributes) {
    super(calcDim(x, y, attributes));
    this.x = x;
    this.y = y;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { x, y };
  }

  @Override
  public String getComputationName() {
    return "crossprod";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingVector(this, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    if (this.crossproduct == null) {
      System.err
          .println("EEK! crossprod.computeCrossProduct() called through getElementAsDouble()");
      //Thread.dumpStack();
      computeCrossProduct();
    }
    return crossproduct[index];
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public int length() {
    int[] dims = getAttributes().getDimArray();
    return dims[0]*dims[1];
  }

  private void computeCrossProduct() {
    this.crossproduct = ((DoubleArrayVector) new MatrixProduct(
        MatrixProduct.CROSSPROD, x, y).crossprod()).toDoubleArrayUnsafe();
  }

  @Override
  public boolean isCalculated() {
    return crossproduct != null;
  }

  @Override
  public Vector forceResult() {
    if (this.crossproduct == null) {
      computeCrossProduct();
    }
    return DoubleArrayVector.unsafe(this.crossproduct);
  }

  @Override
  public void setResult(Vector result) {
    this.crossproduct = ((DoubleArrayVector) result).toDoubleArrayUnsafe();
  }
}
