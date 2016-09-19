package org.renjin.compiler.pipeline.node;

import org.renjin.compiler.pipeline.fusion.LoopKernelCompiler;
import org.renjin.compiler.pipeline.fusion.LoopKernels;
import org.renjin.compiler.pipeline.fusion.kernel.CompiledKernel;
import org.renjin.compiler.pipeline.fusion.kernel.LoopKernel;
import org.renjin.compiler.pipeline.fusion.node.*;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.repackaged.asm.Type;
import org.renjin.sexp.*;

import java.lang.reflect.Method;

/**
 * A {@code {@link DeferredNode} that represents a loop operation, such as {@code sum} or 
 * {@code mean} into which one more vector operations have been fused.
 */
public class FusedNode extends DeferredNode implements Runnable {

  private LoopKernel kernel;
  private LoopNode[] kernelOperands;
  
  private DoubleArrayVector resultVector;
  
  public FusedNode(FunctionNode node) {
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
    
    if(node instanceof FunctionNode) {

      FunctionNode computation = (FunctionNode) node;

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
    
    if(node.getVector() instanceof IntSequence) {
      return new IntSeqNode(inputIndex);
    }
    
    if(node.getVector() instanceof RepDoubleVector) {
      return new RepeatingNode(
          addLoopNode(node.getOperand(0)),
          addLoopNode(node.getOperand(1)));
    }

    if(node.getVector() instanceof DoubleVector) {
      return new DoubleArrayNode(inputIndex, node.getResultVectorType());
    }

    if(node.getVector() instanceof IntVector) {
      return new IntArrayNode(inputIndex, node.getResultVectorType());
    }
    
    if(node.getVector() instanceof LogicalVector) {
      return new IntArrayNode(inputIndex, node.getResultVectorType());
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

  @Override
  public Type getResultVectorType() {
    return Type.getType(DoubleArrayVector.class);
  }

  @Override
  public void run() {

    Vector[] vectorOperands = new Vector[getOperands().size()];
    for (int i = 0; i < vectorOperands.length; i++) {
      vectorOperands[i] = getOperand(i).getVector();
    }
    
    LoopKernelCompiler compiler = new LoopKernelCompiler();
    CompiledKernel compiledKernel = compiler.compile(kernel, kernelOperands);

    double[] result = compiledKernel.compute(vectorOperands);
    resultVector = new DoubleArrayVector(result);
  }
  
  public DoubleArrayVector getVector() {
    if(resultVector == null) {
      throw new IllegalStateException("Not computed yet.");
    }
    return resultVector;
  }
}
