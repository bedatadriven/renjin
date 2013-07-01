package org.renjin.compiler.pipeline.opencl.arg;


import com.google.common.collect.Lists;
import org.renjin.compiler.pipeline.opencl.accessor.OclType;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.Vector;

import java.util.Iterator;
import java.util.List;


/**
 * Maps ArrayVector nodes to Kernel arguments
 */
public class ArgumentList implements Iterable<ArgumentList.InputArgument> {




  public class InputArgument {
    private OclType type;
    private int inputGraphIndex;
    private boolean scalar;

    private String kernelArgumentName;
    private int kernelArgumentIndex;

    private String lengthKernelArgumentName;
    private int lengthKernelArgumentIndex;

    public boolean isScalar() {
      return scalar;
    }

    /**
     * @return  the index of this argument within the
     * kernel argument list
     */
    public int getKernelArgumentIndex() {
      return kernelArgumentIndex;
    }

    /**
     * @return the index of this argument's length within the
     * kernel argument list
     */
    public int getLengthKernelArgumentIndex() {
      return lengthKernelArgumentIndex;
    }

    public OclType getType() {
      return type;
    }

    public int getInputGraphIndex() {
      return inputGraphIndex;
    }

    public String getKernelArgumentName() {
      return kernelArgumentName;
    }

    public String getLengthKernelArgumentName() {
      return lengthKernelArgumentName;
    }
  }

  private List<InputArgument> inputArguments = Lists.newArrayList();
  private List<String> inputFormalList = Lists.newArrayList();

  private OclType resultBufferType;
  private OclType localBufferType;

  public void setResultBufferType(OclType resultBufferType) {
    this.resultBufferType = resultBufferType;
  }

  public void setLocalBufferType(OclType localBufferType) {
    this.localBufferType = localBufferType;
  }


  public OclType getLocalMemoryArgumentType() {
    return localBufferType;
  }


  public int getLocalMemoryArgumentIndex() {
    return inputFormalList.size() + 1;
  }

  public OclType getResultType() {
    return resultBufferType;
  }

  public InputArgument addArgument(int inputGraphIndex, Vector vector) {
    InputArgument arg = new InputArgument();
    arg.inputGraphIndex = inputGraphIndex;
    arg.kernelArgumentName = "arg" + inputArguments.size();
    arg.lengthKernelArgumentName = "arg" + inputArguments.size() + "_length";

    if(vector instanceof DoubleArrayVector) {
      arg.type = OclType.DOUBLE;
    } else if(vector instanceof IntArrayVector) {
      arg.type = OclType.INT;
    } else {
      throw new UnsupportedOperationException(vector.getTypeName());
    }

    if(vector.length() == 1) {
      arg.scalar = true;
    }

    if(arg.scalar) {
      // int arg1
      inputFormalList.add(arg.type.getTypeName() + " " + arg.kernelArgumentName);
      arg.kernelArgumentIndex = inputFormalList.size();

    } else {
      inputFormalList.add("__global const " + arg.type.getTypeName() + "* " + arg.kernelArgumentName);
      arg.kernelArgumentIndex = inputFormalList.size();

      inputFormalList.add("int  " + arg.lengthKernelArgumentName);
      arg.lengthKernelArgumentIndex = inputFormalList.size();
    }
    inputArguments.add(arg);
    return arg;
  }

  @Override
  public Iterator<InputArgument> iterator() {
    return inputArguments.iterator();
  }


  public String toOpenCl() {
    StringBuilder sb = new StringBuilder();
    sb.append("__global " + resultBufferType.getTypeName() + "* result");
    for(String formal : inputFormalList) {
      sb.append(", ").append(formal);
    }
    if(localBufferType != null) {
      sb.append(", ").append("__local " + localBufferType.getTypeName() + "* localBuf");
    }
    return sb.toString();
  }
}
