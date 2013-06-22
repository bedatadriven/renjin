package org.renjin.compiler.pipeline.opencl.arg;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;

import java.util.List;
import java.util.Map;

/**
 * Maps ArrayVector nodes to Kernel arguments
 */
public class ArgumentMap {


  private String resultBufferType;


  private class Argument {
    private String type;
    private int inputGraphIndex;
    public boolean scalar;

    public String bufferName;
    public String bufferLengthName;
  }

  private List<Argument> arguments = Lists.newArrayList();

  private List<String> formalList = Lists.newArrayList();

  private Map<DeferredNode, Argument> nodeMap = Maps.newHashMap();

  public ArgumentMap(DeferredNode node) {
    List<DeferredNode> operands = node.flatten();
    for(DeferredNode operand : operands) {
      if(isArray(operand)) {
        addArgument(operand, operands.indexOf(operand));
      }
    }
  }

  private void addArgument(DeferredNode operand, int inputGraphIndex) {
    Argument arg = new Argument();
    arg.inputGraphIndex = inputGraphIndex;
    arg.bufferName = "arg" + arguments.size();
    arg.bufferLengthName = "arg" + arguments.size() + "_length";

    if(operand.getVector() instanceof DoubleArrayVector) {
      arg.type = "double";
    } else if(operand.getVector() instanceof IntArrayVector) {
      arg.type = "int";
    } else {
      throw new UnsupportedOperationException(operand.getVector().getTypeName());
    }

    if(operand.getVector().length() == 1) {
      arg.scalar = true;
    }

    if(arg.scalar) {
      // int arg1
      formalList.add(arg.type + " " + arg.bufferName);
    } else {
      formalList.add("__global const " + arg.type + "* " + arg.bufferName);
      formalList.add("int  " + arg.bufferLengthName);
    }
    nodeMap.put(operand, arg);
  }

  public String getValueExpr(DeferredNode node) {
    return nodeMap.get(node).bufferName;
  }

  public String getLengthExpr(DeferredNode node) {
    return nodeMap.get(node).bufferLengthName;
  }

  private boolean isArray(DeferredNode operand) {
    return
        operand.getVector() instanceof DoubleArrayVector ||
        operand.getVector() instanceof IntArrayVector;
  }


  public void setResultBufferType(String resultBufferType) {
    this.resultBufferType = resultBufferType;
  }

  public String toOpenCl() {
    StringBuilder sb = new StringBuilder();
    sb.append("__global " + resultBufferType + "* result");
    for(String formal : formalList) {
      sb.append(", ").append(formal);
    }
    return sb.toString();
  }
}
