package org.renjin.compiler.ir.ssa;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.compiler.ir.tree.TreeNode;

import java.util.Collection;
import java.util.Map;

public class VariableMap {

  private Map<LValue, Expression> definitionMap = Maps.newHashMap();
  private Multimap<LValue, BasicBlock> useByBlockMap = HashMultimap.create();
  private Map<LValue, BasicBlock> definedByBlockMap = Maps.newHashMap();

  public VariableMap(ControlFlowGraph cfg) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      for(Statement statement : bb.getStatements()) {
        if(statement instanceof Assignment) {
          addToMap(bb, (Assignment)statement);
        }
        for(int i=0;i!=statement.getRHS().getChildCount();++i) {
          TreeNode uses = statement.getRHS().childAt(i);
          if(uses instanceof LValue) {
            useByBlockMap.put((LValue)uses, bb);
          }
        }
      }
    }
  }

  public Collection<LValue> getVariables() {
    return definitionMap.keySet();
  }

  private void addToMap(BasicBlock bb, Assignment statement) {
    assert !definitionMap.containsKey(statement.getLHS()) : "cfg must be in SSA form";

    definitionMap.put(statement.getLHS(), statement.getRHS());
    definedByBlockMap.put(statement.getLHS(), bb);
  }

  public Expression getDefinition(LValue variable) {
    return definitionMap.get(variable);
  }

  public boolean isUsedOutsideOf(LValue variable, BasicBlock aBlock) {
    for(BasicBlock bb : useByBlockMap.get(variable)) {
      if(bb != aBlock) {
        return true;
      }
    }
    return false;
  }

  public BasicBlock getDefiningBlock(Variable rhs) {
    return definedByBlockMap.get(rhs);
  }

  public boolean isUsed(LValue lhs) {
    return useByBlockMap.containsKey(lhs);
  }
}
