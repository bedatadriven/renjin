package org.renjin.compiler.pipeline.optimize;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class SquareOptimizer implements Optimizer {

  public SquareOptimizer() {

  }

  @Override
  public boolean optimize(DeferredGraph graph, DeferredNode node) {
    if(isEligible(node)) {

      DeferredNode operand = node.getOperand(0);
      node.replaceVector(new Square(operand.getVector(), operand.getVector().getAttributes()));
      node.replaceOperands(operand);
      return true;
    }
    return false;
  }

  private boolean isEligible(DeferredNode node) {
    return node.isComputation() &&
           node.getComputation().getComputationName().equals("*") &&
           node.getOperand(0) == node.getOperand(1);
  }

  public static class Square extends DoubleVector implements DeferredComputation {
    private Vector vector;

    protected Square(Vector vector, AttributeMap attributes) {
      super(attributes);
      this.vector = vector;
    }

    @Override
    public Vector[] getOperands() {
      return new Vector[] { vector };
    }

    @Override
    public String getComputationName() {
      return "sqr";
    }

    @Override
    protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
      return new Square(vector, attributes);
    }

    @Override
    public double getElementAsDouble(int index) {
      return compute(vector.getElementAsDouble(index));
    }

    @Override
    public int length() {
      return vector.length();
    }

    public static double compute(double x) {
      return x * x;
    }
  }

}
