package org.renjin.gnur.xform;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.link.LinkContext;
import org.renjin.gcc.link.LinkSymbol;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Defines relationships between functions
 */
public class CallGraph {


  public class Node {
    private String name;
    private GimpleFunction function;
    private LinkSymbol symbol;
    private List<Node> calling = Lists.newArrayList();
    
    public Node(GimpleCompilationUnit unit, GimpleFunction function) {
      this.function = function;
      this.name = function.getName();
    }

    public Node(LinkSymbol symbol) {
      this.symbol = symbol;
      this.name = symbol.getName();
    }
    
  }

  private LinkContext context;
  private List<GimpleCompilationUnit> units;
  
  private Map<String, Node> globalMap = Maps.newHashMap();

  public CallGraph(LinkContext context, List<GimpleCompilationUnit> units) {
    this.context = context;
    this.units = units;

    // Add all "extern" functions to the global symbol table
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction gimpleFunction : unit.getFunctions()) {
        if(gimpleFunction.isExtern()) {
          globalMap.put(gimpleFunction.getMangledName(), new Node(unit, gimpleFunction));
        }
      }
    }
    
    // Now build links between call sites and the functions they are calling
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction function : unit.getFunctions()) {
        Node callingNode = globalMap.get(function.getMangledName());
        for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
          for (GimpleStatement statement : basicBlock.getStatements()) {
            if(statement instanceof GimpleCall) {
              Node calledNode = findCalledNode(((GimpleCall) statement));
              callingNode.calling.add(calledNode);
            }
          }
        }
      }
    }
  }

  private Node findCalledNode(GimpleCall statement) {
    GimpleExpr function = statement.getFunction();
    if(function instanceof GimpleAddressOf) {
      GimpleExpr value = ((GimpleAddressOf) function).getValue();
      if(value instanceof GimpleFunctionRef) {
        GimpleFunctionRef ref = (GimpleFunctionRef) value;
        Node node = findFunction(ref);
        return node;
      }
    }
    throw new UnsupportedOperationException("function: " + function);
  }

  private Node findFunction(GimpleFunctionRef ref) {
    Node node = globalMap.get(ref.getName());
    if(node == null) {
      LinkSymbol symbol = context.get(ref.getName());
      if(symbol != null) {
        node = new Node(symbol);
        globalMap.put(ref.getName(), node);
      }
    }
    if(node == null) {
      throw new IllegalStateException("Could not find function '" + ref.getName() + "'");
    }
    return node;
  }


  public void dumpGraph(String path) throws FileNotFoundException {
    PrintWriter g = new PrintWriter(path);
    g.println("digraph callgraph {");

    for (Node node : globalMap.values()) {
      for (Node calledNode : node.calling) {
        g.println(node.name + " -> " + calledNode.name + ";");
      }
    }

    g.println("}");
    g.close();

  }
  

}
