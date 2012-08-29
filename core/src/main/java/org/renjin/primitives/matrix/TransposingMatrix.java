package org.renjin.primitives.matrix;


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

public class TransposingMatrix extends DoubleVector implements DeferredComputation {

  public static final int LENGTH_THRESHOLD = 1000;

  private final Vector source;
  private int[] sourceDim;
  private int sourceRowCount;
  private int sourceColCount;

  public TransposingMatrix(Vector source, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceDim = ((IntVector)source.getAttribute(Symbols.DIM)).toIntArray();
    this.sourceRowCount = sourceDim[0];
    this.sourceColCount = sourceDim[1];
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new TransposingMatrix(source, attributes);
  }

  @Override
  public double getElementAsDouble(int vectorIndex) {
    int row = vectorIndex % sourceColCount;
    vectorIndex = (vectorIndex - row) / sourceColCount;
    int col = vectorIndex % sourceRowCount;

    return source.getElementAsDouble(col + (row * sourceRowCount));
  }

  @Override
  public int length() {
    return source.length();
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, new IntArrayVector(sourceRowCount) };
  }

  @Override
  public String getComputationName() {
    return "t";
  }
}
