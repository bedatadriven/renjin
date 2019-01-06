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

import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.BasicBlockEndingStatement;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.util.DebugGraph;

import java.util.*;

public class ControlFlowGraph {

  private final IRBody parent;
  private final List<BasicBlock> basicBlocks;
  private BasicBlock entry;
  private BasicBlock exit;
  private Map<IRLabel, BasicBlock> basicBlockMap = Maps.newHashMap();
  
  public ControlFlowGraph(IRBody body) {
    this.parent = body;
    this.basicBlocks = Lists.newArrayList();
    
    addBasicBlocks();
    linkBasicBlocks();
    removeDeadBlocks();
  }

  private void addBasicBlocks() {

    entry = new BasicBlock(parent);
    entry.setDebugId("entry");
    basicBlocks.add(entry);

    BasicBlock current = null;
    for(int i=0;i<parent.getStatements().size();++i) {
      Statement stmt = parent.getStatements().get(i);
      
      if(current == null || parent.isLabeled(i)) {
        current = addNewBasicBlock(parent, i);
      } else {
        current.addStatement(stmt);
      }
      
      if(stmt instanceof BasicBlockEndingStatement) {
        current = null;
      }
    }

    exit = new BasicBlock(parent);
    exit.setDebugId("exit");
    basicBlocks.add(exit);

    // Cytron adds an edge from entry to exit??
    addEdge(entry, exit);
  }
  
  public List<BasicBlock> getLiveBasicBlocks() {
    return Collections.unmodifiableList(basicBlocks);
  }

  private BasicBlock addNewBasicBlock(IRBody body, int i) {
    BasicBlock bb = BasicBlock.createWithStartAt(body, i);
    bb.setDebugId(basicBlocks.size());
    basicBlocks.add(bb);
    for(IRLabel label : bb.getLabels()) {
      basicBlockMap.put(label, bb);
    }
    return bb;
  } 
  
  private void linkBasicBlocks() {

    // loop over blocks, excluding entry / exit
    for(int i=1;i<basicBlocks.size()-1;++i) {
      BasicBlock bb = basicBlocks.get(i);
      if(bb.fallsThrough()) {
        addEdge(bb, basicBlocks.get(i + 1));
        
      } else if(bb.returns()) {
        addEdge(bb, exit);

      } else {
        for(IRLabel targetLabel : bb.targets()) {
          BasicBlock targetBB = basicBlockMap.get(targetLabel);
          if(targetBB == null) {
            throw new NullPointerException("whoops! no basic block with label '" + targetLabel +
                "' in IRBody " + parent);
          }
          addEdge(bb, targetBB);
        }
      }
    }
    addEdge(entry, basicBlocks.get(1));
  }

  private void addEdge(BasicBlock bb, BasicBlock basicBlock) {
    bb.addFlowSuccessor(basicBlock);
  }

  private void removeDeadBlocks() {
    
    Set<BasicBlock> live = new HashSet<>();
    live.add(entry);
    live.add(basicBlocks.get(1));
    
    boolean changing;
    do {
      changing=false;

      for (BasicBlock basicBlock : Lists.newArrayList(live)) {
        if(live.addAll(basicBlock.getFlowSuccessors())) {
          changing = true;
        }
      }
    } while(changing);
    
    // Clean up nodes and edges
    basicBlocks.retainAll(live);
    for (BasicBlock basicBlock : basicBlocks) {
      basicBlock.removeDeadEdges(live);
    }
    
    int i=1;
    for(BasicBlock bb : basicBlocks) {
      if(bb!=entry && bb!=exit) {
        bb.setDebugId(i++);
      }
    }
  }

  public List<BasicBlock> getBasicBlocks() {
    return basicBlocks;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(BasicBlock bb : basicBlocks) {
      sb.append("\n" + bb.toString());
      if(bb.getLabels() != null) {
        sb.append(": ").append(bb.getLabels());
      }
      sb.append(" =============\n");
      sb.append(bb.statementsToString());
    }
    return sb.toString();
  }

  public BasicBlock getEntry() {
    return entry;
  }

  public BasicBlock getExit() {
    return exit;
  }

  public List<BasicBlock> getSuccessors(BasicBlock x) {
    return x.getFlowSuccessors();
  }

  public List<BasicBlock> getPredecessors(BasicBlock x) {
    return x.getFlowPredecessors();
  }

  public void dumpGraph() {
    DebugGraph dump = new DebugGraph("compute");
    for (BasicBlock basicBlock : basicBlocks) {
      for (BasicBlock successor : basicBlock.getFlowSuccessors()) {
        dump.printEdge(basicBlock.getDebugId(), successor.getDebugId());
      }
    }
    dump.close();
  }

  public void dumpEdges() {
    for (BasicBlock basicBlock : basicBlocks) {
      for (BasicBlock block : basicBlock.getFlowSuccessors()) {
        System.out.println("edge[" + basicBlock.getDebugId() + ", " + block.getDebugId() + "]");
      }
    }
  }

  public BasicBlock get(String debugId) {
    for (BasicBlock basicBlock : basicBlocks) {
      if(basicBlock.getDebugId().equals(debugId)) {
        return basicBlock;
      }
    }
    throw new IllegalArgumentException("No such block: " + debugId);
  }
}
