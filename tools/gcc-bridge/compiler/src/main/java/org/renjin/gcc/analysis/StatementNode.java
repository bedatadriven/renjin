package org.renjin.gcc.analysis;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.GimpleAssignment;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;

import java.util.Iterator;

/**
 * Doubly-linked list of {@code GimpleStatements}.
 */
public class StatementNode {
  private GimpleStatement statement;
  private StatementNode predecessor;
  private StatementNode successor;
  
  private final GimpleVariableRef def;

  public StatementNode(GimpleStatement statement) {
    this.statement = statement;
    this.def = isLhs(statement);
  }

  private GimpleVariableRef isLhs(GimpleStatement statement) {
    if(statement instanceof GimpleAssignment) {
      GimpleAssignment assignment = (GimpleAssignment) statement;
      if(assignment.getLHS() instanceof GimpleVariableRef) {
        return (GimpleVariableRef)assignment.getLHS();
      }
    }
    if(statement instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) statement;
      if(call.getLhs() instanceof GimpleVariableRef) {
        return (GimpleVariableRef) call.getLhs();
      }
    }
    return null;
  }
  
  public boolean uses(GimpleSymbolRef ref) {
    return Iterables.contains(statement.getUsedExpressions(), ref);
  }

  public GimpleStatement getStatement() {
    return statement;
  }

  public static StatementNode createLinkedList(GimpleBasicBlock block) {

    Iterator<GimpleStatement> it = block.getStatements().iterator();
    if(!it.hasNext()) {
      throw new IllegalArgumentException("Empty basic block");
    }
    
    StatementNode head = new StatementNode(it.next());
    StatementNode tail = head;
    
    while(it.hasNext()) {
      StatementNode node = new StatementNode(it.next());
      node.predecessor = tail;
      tail.successor = node;
      tail = node;
    }
    return head;
  }

  public StatementNode getSuccessor() {
    return successor;
  }

  public boolean hasSuccessor() {
    return successor != null;
  }

  /**
   * 
   * @return the successor statement with a definition node, or {@code null} if there 
   * are no remaining defintion statements in this sequence.
   */
  public StatementNode nextDefinition() {
    StatementNode node = successor;
    while(node != null && node.def == null) {
      node = node.successor;
    }
    return node;
  }

  /**
   * 
   * @return this node if it is a definition, otherwise {@code nextDefinition()}
   */
  public StatementNode firstDefinition() {
    if(def != null) {
      return this;
    } else {
      return nextDefinition();
    }
  }

  public GimpleVariableRef getLhs() {
    return def;
  }

  public GimpleExpr nested() {
    if(statement instanceof GimpleAssignment) {
      GimpleAssignment assignment = (GimpleAssignment) statement;
      switch (assignment.getOperator()) {
        // many of these operators have no effect, and we can just unwrap their
        // operand
        case VAR_DECL:
        case NOP_EXPR:
        case PARM_DECL:
        case MEM_REF:
        case ADDR_EXPR:
          // Can only unwrap if casting is not required
          assert assignment.getLHS() != null;
          assert assignment.getOperands().get(0) != null;
          assert assignment.getOperands().get(0).getType() != null : "missing type: " + assignment.getOperands().get(0);
          assert assignment.getLHS().getType() != null;
          if (assignment.getOperands().get(0).getType().equals(assignment.getLHS().getType())) {
            return assignment.getOperands().get(0);
          }

        default:
          GimpleOpExpr expr = new GimpleOpExpr(assignment.getOperator(), assignment.getOperands());
          expr.setType(assignment.getLHS().getType());
          expr.setLine(getLhs().getLine());
          return expr;
      }
    } else if(statement instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) statement;
      return new GimpleCallExpr(call);
    } else {
      throw new InternalCompilerException();
    }
  }

  public boolean replace(GimpleVariableRef lhs, GimpleExpr nested) {
    return statement.replace(Predicates.<GimpleExpr>equalTo(lhs), nested);
  }

  /**
   * Removes the 
   * 
   * @return this statement's predecessor, or its successor if it has no successor
   */
  public StatementNode remove() {
    if(predecessor == null) {
      // no predeccessor, return the successor, after
      // delinking it from this node
      successor.predecessor = null;
      return successor;
    } else {
      // link our predecessor to our successor, eliminating ourself
      predecessor.successor = successor;
      successor.predecessor = predecessor;
      return predecessor;
    }
  }

  @Override
  public String toString() {
    return statement.toString();
  }

}
