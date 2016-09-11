package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Multimap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class UseDefMap {

  /**
   * Map from variables to their definitions
   */
  private Map<LValue, Assignment> assignmentMap = Maps.newHashMap();

  /**
   * Map from a variable to the unique basic block where it is defined.
   */
  private Map<LValue, BasicBlock> defBlockMap = Maps.newHashMap();

  /**
   * Map from variables to the basic blocks which use those variables
   */
  private Multimap<LValue, BasicBlock> useBlockMap = HashMultimap.create();

  /**
   * Map from variables to the basic blocks which use those variables
   */
  private Multimap<LValue, Statement> useStatementMap = HashMultimap.create();

  /**
   * Set of SSA variables that are actually used.
   */
  private final Set<LValue> variableUsages = new HashSet<>();

  /**
   * Map from definitions to outgoing SSA edges.
   */
  private final Multimap<LValue, SsaEdge> ssaEdges = HashMultimap.create();
  private ControlFlowGraph cfg;

  public UseDefMap(ControlFlowGraph cfg) {
    this.cfg = cfg;
    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (Statement statement : basicBlock.getStatements()) {
        if(statement instanceof Assignment) {
          Assignment assignment = (Assignment) statement;
          assignmentMap.put(assignment.getLHS(), assignment);
          defBlockMap.put(assignment.getLHS(), basicBlock);
        }
      }
    }

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (Statement statement : basicBlock.getStatements()) {
        Expression rhs = statement.getRHS();
        if(rhs instanceof LValue) {
          addSsaEdge((LValue) rhs, basicBlock, statement);
          useBlockMap.put((LValue)rhs, basicBlock);
        } else {
          for(int i=0;i!= rhs.getChildCount();++i) {
            TreeNode uses = rhs.childAt(i);
            if(uses instanceof LValue) {
              addSsaEdge((LValue) uses, basicBlock, statement);
              useBlockMap.put((LValue)uses, basicBlock);
            }
          }
        }
      }
    }
  }


  private void addSsaEdge(LValue variable, BasicBlock basicBlock, Statement usage) {
    Assignment definition = assignmentMap.get(variable);
    if(definition != null) {
      SsaEdge edge = new SsaEdge(definition, basicBlock, usage);
      ssaEdges.put(definition.getLHS(), edge);

      if(basicBlock != cfg.getExit()) {
        variableUsages.add(definition.getLHS());
      }
    }
  }

  public boolean isDefined(LValue variable) {
    return assignmentMap.containsKey(variable);
  }

  public Assignment getDefinition(LValue variable) {
    return assignmentMap.get(variable);
  }

  public BasicBlock getDefinitionBlock(LValue a) {
    return defBlockMap.get(a);
  }

  public Collection<SsaEdge> getSsaEdges(LValue lhs) {
    return ssaEdges.get(lhs);
  }

  public boolean isUsed(LValue variable) {
    return variableUsages.contains(variable);
  }

  public Set<LValue> getUsedVariables() {
    return variableUsages;
  }

  public Collection<BasicBlock> getUsedBlocks(LValue a) {
    return useBlockMap.get(a);
  }
}
