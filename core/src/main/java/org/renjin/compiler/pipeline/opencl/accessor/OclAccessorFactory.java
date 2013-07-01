package org.renjin.compiler.pipeline.opencl.accessor;

import com.google.common.collect.Lists;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.arg.ArgumentList;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;

import java.util.List;


public class OclAccessorFactory {

  private ArgumentList arguments;
  private List<DeferredNode> flattened = Lists.newArrayList();

  public OclAccessorFactory(ArgumentList arguments, DeferredNode rootNode) {
    this.arguments = arguments;
    this.flattened = rootNode.flatten();
  }

  public int getIndexOf(DeferredNode node) {
    return flattened.indexOf(node);
  }

  public OclAccessor get(DeferredNode node) {
    // is this node mapped to an input scalar/buffer ?
    if(isArray(node)) {
      // add a kernel argument
      ArgumentList.InputArgument arg = arguments.addArgument(flattened.indexOf(node), node.getVector());
      if(arg.isScalar()) {
        return new OclScalarAccessor(arg.getKernelArgumentName());
      } else {
        return new OclBufferAccessor(arg.getKernelArgumentName(), arg.getLengthKernelArgumentName());
      }
    } else if (OclDistAccessor.accept(node)) {
      return new OclDistAccessor(this, node);
    } else if (OclBinaryOpAccessor.accept(node)) {
      return new OclBinaryOpAccessor(this, node);
    } else if (OclRepAccessor.accept(node)) {
      return new OclRepAccessor(this, node);
    } else if (OclTransposeAccessor.accept(node)) {
      return new OclTransposeAccessor(this, node);
    } else if (OclSquareAccessor.accept(node)) {
      return new OclSquareAccessor(this, node);
    } else if( OclConvertingDoubleAccessor.accept(node)) {
      return new OclConvertingDoubleAccessor(this, node);
    }
    throw new UnsupportedOperationException(node.toString());
  }

  private static boolean isArray(DeferredNode operand) {
    return
        operand.getVector() instanceof DoubleArrayVector ||
            operand.getVector() instanceof IntArrayVector;
  }

}
