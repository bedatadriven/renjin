package org.renjin.primitives.combine;

import org.renjin.sexp.*;

/**
 * Inspects a list of elements to combine to determine the result type
 * and the most efficient merge
 */
class Inspector extends SexpVisitor<Vector.Type> {

  public static final int DEFERRED_THRESHOLD = 2000;
  public static final int DEFERRED_ARGUMENT_LIMIT = 200;

  private boolean recursive = false;

  /**
   * Total number of vectors found
   */
  private int vectorCount = 0;

  /**
   * Total number of elements within all vectors found
   */
  private int elementCount = 0;
  private Vector.Type resultType = Null.VECTOR_TYPE;

  /**
   * Visits each element of {@code ListExp}
   */
  Inspector(boolean recursive) {
    this.recursive = recursive;
  }

  @Override
  public void visit(DoubleVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    vectorCount++;
  }

  @Override
  public void visit(IntVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    vectorCount++;
  }

  @Override
  public void visit(LogicalVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    vectorCount++;
  }

  @Override
  public void visit(Null nullExpression) {
    // ignore
  }

  @Override
  public void visit(StringVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    vectorCount++;
  }

  @Override
  public void visit(ComplexVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    vectorCount++;
  }

  @Override
  public void visit(RawVector vector) {
    resultType = Vector.Type.widest(resultType, vector);
    elementCount += vector.length();
    vectorCount++;
  }

  @Override
  public void visit(ListVector list) {
    if(recursive) {
      acceptAll(list);
    } else {
      resultType = Vector.Type.widest(resultType, list);
      elementCount += list.length();
      vectorCount++;
    }
  }

  @Override
  protected void unhandled(SEXP exp) {
    resultType = Vector.Type.widest(resultType, ListVector.VECTOR_TYPE);
    elementCount++;
    vectorCount++;
  }

  public CombinedBuilder newBuilder() {
    if((resultType == DoubleVector.VECTOR_TYPE ||
        resultType == IntVector.VECTOR_TYPE ||
        resultType == StringVector.VECTOR_TYPE) &&
        (elementCount > DEFERRED_THRESHOLD &&
         vectorCount <= DEFERRED_ARGUMENT_LIMIT)) {

      return new LazyBuilder(resultType, vectorCount);

    } else {
      return new MaterializedBuilder(resultType);
    }
  }

  @Override
  public Vector.Type getResult() {
    return resultType;
  }

}
