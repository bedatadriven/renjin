package r.compiler.cfg;

import java.util.List;
import java.util.Map;

import r.compiler.ir.tac.IRBlock;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.instructions.BasicBlockEndingStatement;
import r.compiler.ir.tac.instructions.Statement;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

public class ControlFlowGraph {

  private final IRBlock parent;
  private final DirectedGraph<BasicBlock, Edge> graph;
  private final List<BasicBlock> basicBlocks;
  private BasicBlock entry;
  private BasicBlock exit;
  private Map<IRLabel, BasicBlock> basicBlockMap = Maps.newHashMap();
  
  public ControlFlowGraph(IRBlock block) {
    this.parent = block;
    this.graph = new DirectedSparseGraph<BasicBlock, Edge>();
    this.basicBlocks = Lists.newArrayList();
    
    addBasicBlocks();
    linkBasicBlocks();
    removeDeadBlocks();
  }

  private void addBasicBlocks() {
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
  }
  
  public List<BasicBlock> getLiveBasicBlocks() {
    return Lists.newArrayList( 
        Iterables.filter(basicBlocks, Predicates.in(graph.getVertices())));
  }

  private BasicBlock addNewBasicBlock(IRBlock block, int i) {
    BasicBlock bb = BasicBlock.createWithStartAt(block, i);
    bb.setIndex(basicBlocks.size());
    basicBlocks.add(bb);
    graph.addVertex(bb);
    if(bb.isLabeled()) {
      basicBlockMap.put(bb.getLabel(), bb);
    }
    return bb;
  } 
  
  private void linkBasicBlocks() {
    entry = basicBlocks.get(0);
    exit = new BasicBlock(parent);
    exit.setIndex(basicBlocks.size());
    
    for(int i=0;i!=basicBlocks.size();++i) {
      BasicBlock bb = basicBlocks.get(i);
      if(bb.fallsThrough()) {
        graph.addEdge(new Edge(false), bb, basicBlocks.get(i+1));
      } else if(bb.returns()) {
        graph.addEdge(new Edge(false), bb, exit);
      } else {
        for(IRLabel targetLabel : bb.targets()) {
          BasicBlock targetBB = basicBlockMap.get(targetLabel);
          int targetBBIndex = basicBlocks.indexOf(targetBB);
          graph.addEdge(new Edge(targetBBIndex <= i), bb, targetBB);
        }
      }
    }
    
    basicBlocks.add(exit);
  }
  
  private void removeDeadBlocks() {
    boolean changed;
    do {
      changed=false;
      
      for(BasicBlock vertex : Lists.newArrayList(graph.getVertices())) {
        if(vertex != entry && graph.inDegree(vertex) == 0) {
          if(graph.removeVertex(vertex)) {
            changed=true;
          }
        }
      }
    } while(changed);
  }

  public List<BasicBlock> getBasicBlocks() {
    return basicBlocks;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    int i=1;
    for(BasicBlock bb : basicBlocks) {
      if(graph.containsVertex(bb)) {
        sb.append("\nBASIC BLOCK " + (i++) + " ====================\n");
        sb.append(bb.statementsToString());
      }
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
}
