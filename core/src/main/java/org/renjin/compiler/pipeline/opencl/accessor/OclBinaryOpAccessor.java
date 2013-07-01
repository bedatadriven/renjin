package org.renjin.compiler.pipeline.opencl.accessor;


import com.google.common.collect.Sets;
import org.renjin.compiler.pipeline.DeferredNode;
import org.renjin.compiler.pipeline.opencl.OclKernelBody;

import java.util.Set;

public class OclBinaryOpAccessor implements OclAccessor {

  private static final Set<String> OPERATORS = Sets.newHashSet("*", "+", "-", "/");

  private final String operator;
  private final OclAccessor x;
  private final OclAccessor y;
  private String length;

  public OclBinaryOpAccessor(OclAccessorFactory oclAccessorFactory, DeferredNode node) {
    operator = node.getComputation().getComputationName();
    x = oclAccessorFactory.get(node.getOperand(0));
    y = oclAccessorFactory.get(node.getOperand(1));
  }

  public static boolean accept(DeferredNode node) {
    return node.isComputation() &&
        node.getComputation().getOperands().length == 2 &&
        OPERATORS.contains(node.getComputation().getComputationName());
  }


  @Override
  public void init(OclKernelBody body) {
    x.init(body);
    y.init(body);
    length = body.tempVar("length");
    body.printlnf("int %s = max(%s, %s);", length, x.length(), y.length());
  }

  @Override
  public String value() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String length() {
    return length;
  }

  @Override
  public String valueAt(OclKernelBody body, String index) {
    String xi = body.tempVar("xi");
    String yi = body.tempVar("yi");
    body.println("int " + xi + " = " + index + " % " + x.length() + ";");
    body.println("int " + yi + " = " + index + " % " + y.length() + ";");
    return "(" + x.valueAt(body, xi) + " " + operator +  " " + y.valueAt(body, yi) + ")";
  }
}
