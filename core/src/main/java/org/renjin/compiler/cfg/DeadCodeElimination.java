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

import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.*;

public class DeadCodeElimination {
  private ControlFlowGraph cfg;
  private UseDefMap useDefMap;
  private final DominanceTree rdf;

  public DeadCodeElimination(ControlFlowGraph cfg, UseDefMap useDefMap) {
    this.cfg = cfg;
    this.useDefMap = useDefMap;
    this.rdf = new DominanceTree(new ReverseControlFlowGraph(cfg));
  }

  public void run() {

    Set<Statement> live = new HashSet<>();

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      if(basicBlock.isLive()) {
        for (Statement statement : basicBlock.getStatements()) {
          if (!statement.isPure()) {
            live.add(statement);
          }
        }
      }
    }

    Queue<Statement> workList = new LinkedList<>(live);

    while(!workList.isEmpty()) {
      Statement s = workList.poll();

      for (Statement d : definers(s)) {
        if(live.add(d)) {
          workList.add(d);
        }
      }

      for (BasicBlock b : controlDependencePredecessors(s.getBasicBlock())) {
        if(!b.getStatements().isEmpty()) {
          Statement last = b.getTerminal();
          if (live.add(last)) {
            workList.add(last);
          }
        }
      }
    }

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      ListIterator<Statement> it = basicBlock.getStatements().listIterator();
      while(it.hasNext()) {
        Statement s = it.next();
        if (!live.contains(s)) {
          it.remove();
        }
      }
    }
  }

  private Collection<BasicBlock> controlDependencePredecessors(BasicBlock basicBlock) {
    return rdf.getFrontier(basicBlock);

  }

  private Iterable<Statement> definers(Statement s) {
    List<Statement> definers = new ArrayList<>();
    s.forEachVariableUsed(v -> {
      Assignment definer = useDefMap.getDefinition(v);
      if(definer != null) {
        definers.add(definer);
      }
    });
    return definers;
  }
}
