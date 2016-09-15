package org.renjin.compiler.pipeline.specialization;

import org.renjin.compiler.pipeline.node.ComputationNode;

import java.util.HashMap;
import java.util.Map;


public enum FunctionSpecializers {

  INSTANCE;

  private Map<String, FunctionSpecializer> map;

  FunctionSpecializers() {
    map = new HashMap<String, FunctionSpecializer>();
    if (System.getProperty("renjin.vp.disablejit") == null) {
      map.put("mean", new SumMeanSpecializer());
      map.put("sum", new SumMeanSpecializer());
      map.put("rowMeans", new RowMeanSpecializer());
      map.put("colSums", new ColSumSpecializer());
    } else {
      System.err.println("Specializers are disabled");
    }
  }

  public boolean supports(ComputationNode node) {
    return map.containsKey(node.getComputationName());
  }

  public FunctionSpecializer get(ComputationNode node) {
    return map.get(node.getComputationName());
  }
}
