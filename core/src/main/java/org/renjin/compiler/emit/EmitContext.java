package org.renjin.compiler.emit;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.objectweb.asm.Label;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.RegisterAllocation;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class EmitContext {

  private Map<IRLabel, Label> labels = Maps.newHashMap();
  private Multimap<LValue, Expression> definitionMap = HashMultimap.create();
  private RegisterAllocation registerAllocation;

  public EmitContext(ControlFlowGraph cfg, RegisterAllocation registerAllocation) {
    this.registerAllocation = registerAllocation;
    buildDefinitionMap(cfg);
  }

  private void buildDefinitionMap(ControlFlowGraph cfg) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      for(Statement stmt : bb.getStatements()) {
        if(stmt instanceof Assignment) {
          Assignment assignment = (Assignment)stmt;
          definitionMap.put(assignment.getLHS(), assignment.getRHS());
        }
      }
    }
  }

  public Label getAsmLabel(IRLabel irLabel) {
    Label asmLabel = labels.get(irLabel);
    if(asmLabel == null) {
      asmLabel = new Label();
      labels.put(irLabel, asmLabel);
    }
    return asmLabel;
  }

  public int getRegister(LValue lValue) {
    return registerAllocation.getRegister(lValue);
  }
}
