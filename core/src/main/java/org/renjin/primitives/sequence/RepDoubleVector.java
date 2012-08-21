package org.renjin.primitives.sequence;


import org.renjin.sexp.*;

public class RepDoubleVector extends DoubleVector {

  public static final int LENGTH_THRESHOLD = 100;

  private final DoubleVector source;
  private int length;
  private int each;

  private RepDoubleVector(DoubleVector source, int length, int each, PairList attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }

  public RepDoubleVector(DoubleVector source, int length, int each) {
    this(source, length, each, transformAttributes(source, length, each));
  }

  private static PairList transformAttributes(DoubleVector source, int length, int each) {
    PairList.Builder builder = new PairList.Builder();
    for(PairList.Node node : source.getAttributes().nodes()) {
      if(node.getTag() == Symbols.NAMES) {
        builder.add(Symbols.NAMES, new RepStringVector((Vector)node.getValue(), length, each, Null.INSTANCE));
      } else {
        builder.add(node.getTag(), node.getValue());
      }
    }
    return builder.build();
  }

  @Override
  protected SEXP cloneWithNewAttributes(PairList attributes) {
    return new RepDoubleVector(source, length, each, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return source.getElementAsDouble((index / each) % source.length());
  }

  @Override
  public int length() {
    return length;
  }
}
