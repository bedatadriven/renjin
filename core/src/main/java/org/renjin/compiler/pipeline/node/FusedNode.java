package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.fusion.LoopKernels;
import org.renjin.compiler.pipeline.fusion.kernel.LoopKernel;
import org.renjin.compiler.pipeline.fusion.node.*;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntBufferVector;
import org.renjin.sexp.IntVector;

import java.lang.reflect.Method;

/**
 * A {@code {@link DeferredNode} that represents a loop operation, such as {@code sum} or 
 * {@code mean} into which one more vector operations have been fused.
 */
public class FusedNode extends DeferredNode {

  private LoopKernel kernel;
  private LoopNode[] kernelOperands;
  
  public FusedNode(ComputationNode node) {
    super();
    
    this.kernel = LoopKernels.INSTANCE.get(node);
    this.kernelOperands = new LoopNode[node.getOperands().size()];

    for (int i = 0; i < kernelOperands.length; i++) {
      kernelOperands[i] = addLoopNode(node.getOperand(i));
    }
  }
  
  private LoopNode addLoopNode(DeferredNode node) {


    // If this Deferred is a binary or unary vector operator, then 
    // we can inline into the loop
    
    if(node instanceof ComputationNode) {

      ComputationNode computation = (ComputationNode) node;

      int arity = node.getOperands().size();

      if (arity == 1) {
        Method unaryOperator = UnaryVectorOpNode.findMethod(node.getVector());
        if (unaryOperator != null) {
          return new UnaryVectorOpNode(
              computation.getComputationName(), 
              unaryOperator, addLoopNode(node.getOperand(0)));
        }
      }

      if (arity == 2) {
        Method binaryOperator = BinaryVectorOpNode.findMethod(node.getVector());
        if (binaryOperator != null) {
          return new BinaryVectorOpNode(
              computation.getComputationName(),
              binaryOperator,
              addLoopNode(node.getOperand(0)),
              addLoopNode(node.getOperand(1)));
        }
      }
    }
    
    
    // Otherwise we expect that it will be computed and stored
    // into an array before this DeferredNode is evaluated.

    return addLoopInput(node);
  }

  private LoopNode addLoopInput(DeferredNode node) {
    int inputIndex = this.addInput(node);
    node.addOutput(this);

    if(node.getVector() instanceof IntBufferVector) {
      return new IntBufferNode(inputIndex);
    }

    if(node.getVector() instanceof DoubleVector) {
      return new DoubleArrayNode(inputIndex);
    }

    if(node.getVector() instanceof IntVector) {
      return new IntArrayNode(inputIndex);
    }

    throw new UnsupportedOperationException("operand: " + node.getVector().getClass().getName());
  }
  
  @Override
  public String getDebugLabel() {
    return kernel.debugLabel(kernelOperands);
  }

  @Override
  public NodeShape getShape() {
    return NodeShape.ELLIPSE;
  }

}
