package org.renjin.gcc.codegen;

import org.objectweb.asm.Label;
import org.renjin.gcc.gimple.GimpleBasicBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps basic blocks to asm {@link org.objectweb.asm.Label}s
 */
public class Labels {

  private Map<Integer, Label> map = new HashMap<Integer, Label>();
  
  public Label of(GimpleBasicBlock block) {
    return of(block.getIndex());
  }
  
  public Label of(int index) {
    Label label = map.get(index);
    if(label == null) {
      label = new Label();
      map.put(index, label);
    }
    return label;
  }
  
}
