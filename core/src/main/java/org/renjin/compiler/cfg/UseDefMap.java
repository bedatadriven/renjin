/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
          addUse(basicBlock, statement, (LValue) rhs);
        } else {
          for(int i=0;i!= rhs.getChildCount();++i) {
            TreeNode uses = rhs.childAt(i);
            if(uses instanceof LValue) {
              addUse(basicBlock, statement, (LValue)uses);
            }
          }
        }
      }
    }
  }

  private void addUse(BasicBlock basicBlock, Statement statement, LValue rhs) {
    addSsaEdge(rhs, basicBlock, statement);
    useBlockMap.put(rhs, basicBlock);
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
