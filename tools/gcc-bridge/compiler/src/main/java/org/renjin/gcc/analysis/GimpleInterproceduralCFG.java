/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.gcc.analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import heros.InterproceduralCFG;
import org.renjin.gcc.DefaultEntryPointPredicate;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleSymbolTable;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleReturn;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.repackaged.guava.base.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GimpleInterproceduralCFG implements InterproceduralCFG<GimpleNode, GimpleFunction> {

  private final List<GimpleFunction> entryPoints;
  private final Multimap<GimpleFunction, GimpleNode> nodeMap = HashMultimap.create();
  private final Map<GimpleFunction, GimpleNode> startPoints = new HashMap<>();

  public GimpleInterproceduralCFG(List<GimpleCompilationUnit> units, Predicate<GimpleFunction> entryPointPredicate) {
    this.entryPoints = findEntryPoints(units, entryPointPredicate);
    buildGraph(units);
  }

  private void buildGraph(List<GimpleCompilationUnit> units) {
    GimpleSymbolTable symbolTable = new GimpleSymbolTable(units);

    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {

        GimpleSymbolTable.Scope scope = symbolTable.scope(function);

        // First, create a node for every statement in the function

        List<GimpleNode> nodes = new ArrayList<>();
        Map<Integer, GimpleNode> blockStarts = new HashMap<>();
        GimpleNode predecessor = null;

        for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
          boolean blockStart = true;
          for (GimpleStatement statement : basicBlock.getStatements()) {
            GimpleNode node = new GimpleNode(function, statement);

            if(node.getStatement() instanceof GimpleCall) {
              GimpleCall call = (GimpleCall) node.getStatement();
              if(call.isFunctionPointerCall()) {
                throw new UnsupportedOperationException("TODO: function pointers");
              }
              Optional<GimpleFunction> calledFunction = scope.lookupFunction(call.getFunctionRef());
              if(calledFunction.isPresent()) {
                node.callees.add(calledFunction.get());
              }
            }

            if (blockStart) {
              blockStarts.put(basicBlock.getIndex(), node);
              blockStart = false;
            }

            if (predecessor != null && predecessor.getStatement().getJumpTargets().isEmpty()) {
              node.predecessors.add(predecessor);
              predecessor.successors.add(node);
            }

            nodes.add(node);
            predecessor = node;
          }
        }

        // Now link the jump statements
        for (GimpleNode node : nodes) {
          for (Integer targetBlockIndex : node.getStatement().getJumpTargets()) {
            GimpleNode targetNode = blockStarts.get(targetBlockIndex);
            node.successors.add(targetNode);
            targetNode.predecessors.add(node);
          }
        }
        startPoints.put(function, nodes.get(0));
      }
    }
  }

  private static List<GimpleFunction> findEntryPoints(List<GimpleCompilationUnit> units, Predicate<GimpleFunction> entryPointPredicate) {
    List<GimpleFunction> entryPoints = new ArrayList<>();
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        if(entryPointPredicate.apply(function)) {
          entryPoints.add(function);
        }
      }
    }
    return entryPoints;
  }

  public GimpleInterproceduralCFG(List<GimpleCompilationUnit> units) {
    this(units, new DefaultEntryPointPredicate());
  }

  public List<GimpleFunction> getEntryPoints() {
    return entryPoints;
  }

  @Override
  public GimpleFunction getMethodOf(GimpleNode statement) {
    return statement.getFunction();
  }

  @Override
  public List<GimpleNode> getPredsOf(GimpleNode u) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public List<GimpleNode> getSuccsOf(GimpleNode statement) {
    return statement.getSuccessors();
  }

  @Override
  public Collection<GimpleFunction> getCalleesOfCallAt(GimpleNode statement) {
    return statement.getCallees();
  }

  @Override
  public Collection<GimpleNode> getCallersOf(GimpleFunction function) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Set<GimpleNode> getCallsFromWithin(GimpleFunction function) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Collection<GimpleNode> getStartPointsOf(GimpleFunction function) {
    return Collections.singleton(startPoints.get(function));
  }

  @Override
  public Collection<GimpleNode> getReturnSitesOfCallAt(GimpleNode statement) {
    return statement.getSuccessors();
  }

  @Override
  public boolean isCallStmt(GimpleNode stmt) {
    return stmt.isCall();
  }

  @Override
  public boolean isExitStmt(GimpleNode stmt) {
    return stmt.getStatement() instanceof GimpleReturn;
  }

  @Override
  public boolean isStartPoint(GimpleNode stmt) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Set<GimpleNode> allNonCallStartNodes() {
    Set<GimpleNode> set = new HashSet<>();
    for (GimpleNode node : nodeMap.values()) {
      if(!node.isCall() && !node.isCall()) {
        set.add(node);
      }
    }
    return set;
  }

  @Override
  public boolean isFallThroughSuccessor(GimpleNode stmt, GimpleNode succ) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isBranchTarget(GimpleNode stmt, GimpleNode succ) {
    throw new UnsupportedOperationException("TODO");
  }
}
