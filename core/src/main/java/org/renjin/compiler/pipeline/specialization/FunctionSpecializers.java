package org.renjin.compiler.pipeline.specialization;

import org.renjin.compiler.pipeline.DeferredNode;

import java.util.HashMap;
import java.util.Map;


public enum FunctionSpecializers {
    
    INSTANCE;
    
    private Map<String, FunctionSpecializer> map;
    
    FunctionSpecializers() {
        map = new HashMap<String, FunctionSpecializer>();
        map.put("mean", new SumMeanSpecializer());
        map.put("sum", new SumMeanSpecializer());
        map.put("rowMeans", new RowMeanSpecializer());
      //  map.put("colSums", new ColSumSpecializer());
    }
    
    public boolean supports(DeferredNode node) {
        return map.containsKey(node.getComputation().getComputationName());
    }
    
    public FunctionSpecializer get(DeferredNode node) {
        return map.get(node.getComputation().getComputationName());
    }
}
