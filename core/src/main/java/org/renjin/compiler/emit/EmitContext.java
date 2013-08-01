package org.renjin.compiler.emit;


import com.google.common.collect.Maps;
import org.objectweb.asm.Label;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.ir.tac.IRLabel;

import java.util.Map;

public class EmitContext {
  
  private Map<IRLabel, Label> labels = Maps.newHashMap();

  public EmitContext() {
  }
  
  public Label getAsmLabel(IRLabel irLabel) {
    Label asmLabel = labels.get(irLabel);
    if(asmLabel == null) {
      asmLabel = new Label();
      labels.put(irLabel, asmLabel);
    }
    return asmLabel;
  }
}
