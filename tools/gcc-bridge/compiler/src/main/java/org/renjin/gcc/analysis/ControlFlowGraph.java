package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.statement.GimpleEdge;
import org.renjin.repackaged.guava.collect.Iterators;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.PeekingIterator;
import org.renjin.repackaged.guava.escape.Escaper;
import org.renjin.repackaged.guava.escape.Escapers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Models control flow between basic blocks.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Control_flow_graph">Control Flow Graphs</a> on Wikipedia
 */
public class ControlFlowGraph {

  /**
   * 
   */
  public class Node {
    private String id;
    private GimpleBasicBlock basicBlock;
    private List<Node> incoming = Lists.newArrayList();
    private List<Node> outgoing = Lists.newArrayList();

    public Node(String id) {
      this.id = id;
    }

    public Node(GimpleBasicBlock basicBlock) {
      this.basicBlock = basicBlock;
      this.id = "BB" + basicBlock.getIndex();
    }

    public String getId() {
      return id;
    }

    public GimpleBasicBlock getBasicBlock() {
      return basicBlock;
    }

    public List<Node> getIncoming() {
      return incoming;
    }

    public List<Node> getOutgoing() {
      return outgoing;
    }

    @Override
    public String toString() {
      return "<" + id + ">";
    }
  }
  
  private Node entryNode = new Node("Entry");
  private Node exitNode = new Node("Exit");
  
  private Map<Integer, Node> nodes = Maps.newHashMap();

  /**
   * Creates a new {@code ControlFlowGraph} from a {@code  GimpleFunction}.
   * 
   */
  public ControlFlowGraph(GimpleFunction function) {
    // Create nodes
    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      nodes.put(basicBlock.getIndex(), new Node(basicBlock));
    }
    
    // create edge from entry to first block
    addEdge(entryNode, getNode(function.getBasicBlocks().get(0)));
    
    // Create edges between block
    PeekingIterator<GimpleBasicBlock> it = Iterators.peekingIterator(function.getBasicBlocks().iterator());
    while(it.hasNext()) {
      GimpleBasicBlock sourceBlock = it.next();
      Node sourceNode = nodes.get(sourceBlock.getIndex());

      List<GimpleEdge> jumps = sourceBlock.getJumps();
      if(jumps.isEmpty()) {
        // fall through edge
        if(it.hasNext()) {
          addEdge(sourceNode, getNode(it.peek()));
        }
      } else {
        // explicit jumps
        Node targetNode;
        for (GimpleEdge jump : jumps) {
          sourceNode = nodes.get(jump.getSource());
          if(jump.getTarget() == 1) {
            targetNode = exitNode;
          } else {
            targetNode = nodes.get(jump.getTarget());
          }
          addEdge(sourceNode, targetNode);
        }
      }
    }
  }

  /**
   * 
   * @return all the nodes in this {@code ControlFlowGraph}. There is a node for each
   * basic block in the function, as well as entry and exit node.
   */
  public Iterable<Node> getNodes() {
    Set<Node> nodes = new HashSet<>();
    nodes.add(entryNode);
    nodes.add(exitNode);
    nodes.addAll(this.nodes.values());
    return nodes;
  }
  
  public Node getNode(GimpleBasicBlock bb) {
    return nodes.get(bb.getIndex());
  }
  
  private void addEdge(Node from, Node to) {
    from.outgoing.add(to);
    to.incoming.add(from);
  }
  
  public void dumpGraph(File file) throws IOException {
    try(PrintWriter writer = new PrintWriter(new FileWriter(file))) {
      dumpGraph(writer);
    }
  }
  
  public Collection<Node> getBasicBlockNodes() {
    return nodes.values();
  }
  
  public void dumpGraph(PrintWriter writer) {

    Escaper escaper = Escapers.builder()
        .addEscape('"', "\\\"")
        .addEscape('\n', "\\n")
        .build();
    
    writer.println("digraph {");

    writer.println(String.format("%s[label=\"Entry\"]", entryNode.id));
    writer.println(String.format("%s[label=\"Exit\"]", exitNode.id));
    

    for (Node node : nodes.values()) {
      writer.println(String.format("%s[label=\"%s\", shape=\"rect\"]", node.id, escaper.escape(node.basicBlock.toString())));
    }

    for (Node node : getNodes()) {
      for (Node out : node.outgoing) {
        writer.println(node.id + " -> " + out.id);
      }
    }
    
    writer.println("}");

  }
}
