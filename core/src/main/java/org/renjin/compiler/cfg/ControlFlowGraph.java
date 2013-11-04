package org.renjin.compiler.cfg;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.statements.BasicBlockEndingStatement;
import org.renjin.compiler.ir.tac.statements.Statement;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import org.renjin.util.DebugGraph;

public class ControlFlowGraph {

  private final IRBody parent;
  private final DirectedGraph<BasicBlock, Edge> graph;
  private final List<BasicBlock> basicBlocks;
  private BasicBlock entry;
  private BasicBlock exit;
  private Map<IRLabel, BasicBlock> basicBlockMap = Maps.newHashMap();
  
  public ControlFlowGraph(IRBody body) {
    this.parent = body;
    this.graph = new DirectedSparseGraph<BasicBlock, Edge>();
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
  }
  
  public List<BasicBlock> getLiveBasicBlocks() {
    return Collections.unmodifiableList(basicBlocks);
  }

  private BasicBlock addNewBasicBlock(IRBody body, int i) {
    BasicBlock bb = BasicBlock.createWithStartAt(body, i);
    bb.setDebugId(basicBlocks.size());
    basicBlocks.add(bb);
    graph.addVertex(bb);
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
        graph.addEdge(new Edge(false), bb, basicBlocks.get(i+1));
      } else if(bb.returns()) {
        graph.addEdge(new Edge(false), bb, exit);
      } else {
        for(IRLabel targetLabel : bb.targets()) {
          BasicBlock targetBB = basicBlockMap.get(targetLabel);
          if(targetBB == null) {
            throw new NullPointerException("whoops! no basic block with label '" + targetLabel +
                "' in IRBody " + parent);
          }
          int targetBBIndex = basicBlocks.indexOf(targetBB);
          graph.addEdge(new Edge(targetBBIndex <= i), bb, targetBB);
        }
      }
    }
//    graph.addEdge(new Edge(false), entry, exit);
    graph.addEdge(new Edge(false), entry, basicBlocks.get(1));
  }
  
  private void removeDeadBlocks() {
    boolean changed;
    do {
      changed=false;
      
      for(BasicBlock vertex : Lists.newArrayList(graph.getVertices())) {
        if(vertex != entry && graph.inDegree(vertex) == 0) {
          if(graph.removeVertex(vertex)) {
          }
          changed=true;
        }
      }
    } while(changed);
    
    basicBlocks.retainAll(graph.getVertices());
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

  public Graph<BasicBlock, Edge> getGraph() {
    return graph;
  }

  public BasicBlock getEntry() {
    return entry;
  }

  public BasicBlock getExit() {
    return exit;
  }

  public Collection<BasicBlock> getSuccessors(BasicBlock x) {
    return graph.getSuccessors(x);
  }

  public Collection<BasicBlock> getPredecessors(BasicBlock x) {
    return graph.getPredecessors(x);
  }

  public void dumpGraph() {
    DebugGraph dump = new DebugGraph("compute");
    for(Edge edge : graph.getEdges()) {
      dump.printEdge(graph.getSource(edge).getDebugId(), graph.getDest(edge).getDebugId());
    }
    dump.close();

  }
}
