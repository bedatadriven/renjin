package org.renjin.primitives.matrix;


import org.renjin.sexp.*;

public class TransposedMatrix extends DoubleVector {

  public static final int LENGTH_THRESHOLD = 1000;

  private final Vector source;
  private int[] sourceDim;
  private int sourceRowCount;
  private int sourceColCount;

  private TransposedMatrix(Vector source,PairList attributes) {
    super(attributes);
    this.source = source;
    this.sourceDim = ((IntVector)source.getAttribute(Symbols.DIM)).toIntArray();
    this.sourceRowCount = sourceDim[0];
    this.sourceColCount = sourceDim[1];
  }

  public TransposedMatrix(Vector source) {
    this(source, transformAttributes(source.getAttributes()));
  }

  private static PairList transformAttributes(PairList attributes) {
    PairList.Builder copy = new PairList.Builder();
    for(PairList.Node node : attributes.nodes()) {
      if(node.getTag() == Symbols.DIM) {
        int dim[] = ((IntVector)node.getValue()).toIntArray();
        assert dim.length == 2;
        copy.add(Symbols.DIM, new IntArrayVector(dim[1], dim[0]));
      } else {
        copy.add(node.getTag(), node.getValue());
      }
    }
    return copy.build();
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new TransposedMatrix(source, attributes);
  }

  @Override
  public double getElementAsDouble(int vectorIndex) {
    int row = vectorIndex % sourceColCount;
    vectorIndex = (vectorIndex - row) / sourceColCount;
    int col = vectorIndex % sourceRowCount;

    return source.getElementAsDouble( col + (row * sourceRowCount) );
  }

  @Override
  public int length() {
    return source.length();
  }
}
