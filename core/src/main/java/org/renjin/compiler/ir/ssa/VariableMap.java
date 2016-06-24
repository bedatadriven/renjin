package org.renjin.compiler.ir.ssa;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.TypeBounds;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class VariableMap {

  private Map<LValue, Expression> definitionMap = Maps.newHashMap();
  private Multimap<LValue, BasicBlock> useByBlockMap = HashMultimap.create();
  private Map<LValue, BasicBlock> definedByBlockMap = Maps.newHashMap();

  private Map<LValue, TypeBounds> typeMap = Maps.newHashMap();

  public VariableMap(ControlFlowGraph cfg) {
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      for(Statement statement : bb.getStatements()) {
        if(statement instanceof Assignment) {
          addToMap(bb, (Assignment)statement);
        }
        Expression rhs = statement.getRHS();
        if(rhs instanceof LValue) {
          useByBlockMap.put((LValue)rhs, bb);
        } else {
          for(int i=0;i!= rhs.getChildCount();++i) {
            TreeNode uses = rhs.childAt(i);
            if(uses instanceof LValue) {
              useByBlockMap.put((LValue)uses, bb);
            }
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

  public boolean isDefined(LValue value) {
    return definitionMap.containsKey(value);
  }


  public void resolveTypes() {

    // Iteratively update the bounds of variables to the union of their definitions's types
    boolean changing;
    do {
      changing = false;
      for (LValue variable : getVariables()) {
        TypeBounds oldBounds = typeMap.get(variable);
        TypeBounds newBounds;

        try {
          Expression definition = definitionMap.get(variable);
          newBounds = definition.computeTypeBounds(typeMap);
          System.out.printf("variable %s = %s = %s%n", variable, definition, newBounds);
        } catch (Exception e) {
          throw new IllegalStateException("Exception updating bounds for " + variable, e);
        }

        if(!Objects.equals(oldBounds, newBounds)) {
          typeMap.put(variable, newBounds);
          changing = true;
        }
      }
    } while (changing);
  }
}
