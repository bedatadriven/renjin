package org.renjin.compiler;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.FlowEdge;
import org.renjin.compiler.cfg.SsaEdge;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.NullExpression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.IfStatement;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.*;

/**
 * Propagates constant values <strong>and</strong> type properties through a CFG in 
 * order to further specialize the CFG.
 * 
 * <p>This class uses <em>Sparse conditional constant propagation</em> to propagate 
 * both constant values as well as type information like vector type, length, presence of NAs,
 * etc, throughout as much of the code as possible.
 * 
 * <p>The implementation is implemented based on the paper Wegman, Mark N. and Zadeck, F. Kenneth. 
 * "Constant Propagation with Conditional Branches." ACM Transactions on Programming Languages and Systems,
 * 13(2), April 1991, pages 181-210.</a>
 * 
 * @see <a href="https://www.cs.utexas.edu/users/lin/cs380c/wegman.pdf">Constant Propagation with Conditional Branches</a>
 * @see <a href="http://lampwww.epfl.ch/resources/lamp/teaching/advancedCompiler/2005/slides/05-UsingSSA_CP-1on1.pdf">
 *   Slides on Constant Propagation on SSA form</a>
 */
public class TypeSolver {
  
  // http://lampwww.epfl.ch/resources/lamp/teaching/advancedCompiler/2005/slides/05-UsingSSA_CP-1on1.pdf
  // https://www.cs.utexas.edu/users/lin/cs380c/wegman.pdf

  private final ControlFlowGraph cfg;
  /**
   * Map from variables to their definitions
   */
  private Map<LValue, Assignment> definitionMap = Maps.newHashMap();
  
  /**
   * Map from definitions to outgoing SSA edges.
   */
  private final Multimap<LValue, SsaEdge> ssaEdges = HashMultimap.create();


  private final ArrayDeque<FlowEdge> flowWorkList = new ArrayDeque<>();
  private final ArrayDeque<SsaEdge> ssaWorkList = new ArrayDeque<>();

  /**
   * Set of SSA variables that are actually used.
   */
  private final Set<LValue> variableUsages = new HashSet<>();

  private static final ValueBounds TOP = null;
  
  private final Map<Expression, ValueBounds> lattice = Maps.newHashMap();
  
  public TypeSolver(ControlFlowGraph cfg) {
    this.cfg = cfg;
    buildSsaEdges();
    execute();
  }

  private void buildSsaEdges() {

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (Statement statement : basicBlock.getStatements()) {
        if(statement instanceof Assignment) {
          Assignment assignment = (Assignment) statement;
          definitionMap.put(assignment.getLHS(), assignment);
        }
      }
    }

    for (BasicBlock basicBlock : cfg.getBasicBlocks()) {
      for (Statement statement : basicBlock.getStatements()) {
        Expression rhs = statement.getRHS();
        if(rhs instanceof LValue) {
          addSsaEdge((LValue) rhs, basicBlock, statement);
        } else {
          for(int i=0;i!= rhs.getChildCount();++i) {
            TreeNode uses = rhs.childAt(i);
            if(uses instanceof LValue) {
              addSsaEdge((LValue) uses, basicBlock, statement);
            }
          }
        }
      }
    }
  }

  public boolean isDefined(LValue variable) {
    return definitionMap.containsKey(variable);
  }
  
  public boolean isUsed(Assignment assignment) {
    return isUsed(assignment.getLHS());
  }

  public boolean isUsed(LValue variable) {
    return variableUsages.contains(variable);
  }
  
  private void addSsaEdge(LValue variable, BasicBlock basicBlock, Statement usage) {
    Assignment definition = definitionMap.get(variable);
    if(definition != null) {
      SsaEdge edge = new SsaEdge(definition, basicBlock, usage);
      ssaEdges.put(definition.getLHS(), edge);
    
      if(basicBlock != cfg.getExit()) {
        variableUsages.add(definition.getLHS());
      }
    }
  }
  
  public Map<LValue, ValueBounds> getVariables() {
    Map<LValue, ValueBounds> map = new HashMap<>();
    for (LValue variable : variableUsages) {
      map.put(variable, lattice.get(variable));
    }
    return map;
  }

  private void execute() {

    // (1) Initialize the flowWorkList to contain the edges exiting the start node of the program. 
    //     The SSA Work list is initially empty
    for (FlowEdge flowEdge : cfg.getEntry().getOutgoing()) {
      if(flowEdge.getSuccessor() != cfg.getExit()) {
        flowWorkList.add(flowEdge);
      }
    }

    // (2) Halt execution when both worklists become empty. Execution
    //     may proceed by processing items from either worklist

    while(! (flowWorkList.isEmpty() && ssaWorkList.isEmpty())) {


      while(!flowWorkList.isEmpty()) {

        // (3) If the item is a program flow edg from the flowWorkList, then 
        //     examine the Executable falg of that edge. If the ExecutableFlag is true
        //     do nothing; otherwise:

        FlowEdge edge = flowWorkList.pop();
        if(!edge.isExecutable()) {
          BasicBlock node = edge.getSuccessor();
          
          // (a) mark the executable node as true

          edge.setExecutable(true);

          // (b) Perform Visit-phi for all of the phi functions at the destination node

          for (Assignment phiAssignment : node.phiAssignments()) {
            visitPhi(phiAssignment);
          }

          // (c) If only one of the ExecutableFlags associated with the incoming
          //     program flow graph edges is true (i.e. this the first time this
          //     node has been evaluated), then perform VisitExpression for all expressions
          //     in this node.

          if(node.countIncomingExecutableEdges() == 1) {
            for (Statement statement : node.getStatements()) {
              if(statement.getRHS() != NullExpression.INSTANCE &&
                 !(statement.getRHS() instanceof PhiFunction)) {
                visitExpression(node, statement);
              }
            }
          }

          // (d) If then node only contains one outgoing flow edge, add that edge to the
          //     flowWorkList

          if(node.getOutgoing().size() == 1) {
            flowWorkList.addAll(node.getOutgoing());
          }
        }
      }


      while(!ssaWorkList.isEmpty()) {

        SsaEdge edge = ssaWorkList.pop();

        // (4) If the item is an SSA edge from the SSAWorkList and the destination of that 
        //     edge is a phi-function, perform visit-phi

        // (5) If the item is an SSA edge from the SSA Work list and the destination of that
        //     edge is an expression, then examine ExecutableFlags for the program flow edges
        //     reaching that node. If any of them are true, perform VisitExpression. 
        //     Otherwise do nothing.
        
        if(edge.isPhiFunction()) {
          visitPhi((Assignment) edge.getDestinationStatement());
        
        } else if(edge.getDestinationNode().countIncomingExecutableEdges() > 0) {
          visitExpression(edge.getDestinationNode(), edge.getDestinationStatement());
        }
      }
    }
  }

  private void visitPhi(Assignment assignment) {
    
    PhiFunction phiFunction = (PhiFunction) assignment.getRHS();
    
    // The LatticeCells for each operand of the phi-function are defined
    // on the basis of the ExecutableFlag for the corresponding program flow edge.

    // executable: The LatticeCell has the same value as the LatticeCell at the definition
    // end of the of the SSA edge
    
    // non-executable: The LatticeCell has the value TOP

    List<ValueBounds> boundSet = new ArrayList<>();
    for (int i = 0; i < phiFunction.getIncomingEdges().size(); i++) {
      FlowEdge incomingEdge = phiFunction.getIncomingEdges().get(i);
      if(incomingEdge.isExecutable()) {
        Variable ssaVariable = phiFunction.getArgument(i);
        ValueBounds value = lattice.get(ssaVariable);
        if(value != TOP) {
          boundSet.add(value);
        }
      }
    }
    
    if(!boundSet.isEmpty()) {
      lattice.put(assignment.getLHS(), ValueBounds.union(boundSet));
    }
  }
  
  public void dumpBounds() {
    for (Expression expression : lattice.keySet()) {
      if(expression instanceof LValue) {
        System.out.println(expression + " => " + lattice.get(expression));
      }
    }
  }
  
  private void visitExpression(BasicBlock block, Statement statement) {
    // Evaluate the expression obtaining the values of the operands from the LatticeCells
    // where they are defined and using the expression rules in Section 2.2


    Expression expression = statement.getRHS();
    ValueBounds oldBounds = lattice.get(expression);
    ValueBounds newBounds = expression.updateTypeBounds(lattice);
    
    if(!Objects.equals(oldBounds, newBounds)) {

      lattice.put(expression, newBounds);
      
      // If this changes the value of the LatticeCell of the output expression, do the following:

      // (1) If the expression is part of an assignment node, add to the SSA worklist all
      //     SSA edges starting at the definition for that node.

      if(statement instanceof Assignment) {
        Assignment assignment = (Assignment) statement;
        Collection<SsaEdge> outgoingEdges = ssaEdges.get(assignment.getLHS());

        lattice.put(assignment.getLHS(), newBounds);
        ssaWorkList.addAll(outgoingEdges);
      }

      // (2) If the expression controls a conditional branch, some outgoing flow graph
      //     edges must be added to the Flow Work List. If the LatticeCell has  value BOT,
      //     all exit edges must be added to the FlowWorkList. If the value is CONSTANT, 
      //     only the flow graph edge executed as the result of the branch is added
      //     to the FlowWorkList.
      
      if(statement instanceof IfStatement) {
        flowWorkList.addAll(block.getOutgoing());
      }
    }
  }

}
