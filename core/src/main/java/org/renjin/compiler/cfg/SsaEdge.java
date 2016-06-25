package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

/**
 * Each {@code SsaEdge} goes from the unique point where a variable is given a value
 * to a use of that variable. SSA Edges are essentially def-use chains in the SSA program.
 */
public class SsaEdge {
  
  private Assignment definition;
  private Statement destinationStatement;
  private BasicBlock destinationNode;
    
  public SsaEdge(Assignment definition, BasicBlock usageNode, Statement statement) {
    this.definition = definition;
    this.destinationNode = usageNode;
    this.destinationStatement = statement;
  }

  public Assignment getDefinition() {
    return definition;
  }

  public BasicBlock getDestinationNode() {
    return destinationNode;
  }

  public Statement getDestinationStatement() {
    return destinationStatement;
  }
  
  public Expression getDestination() {
    return destinationStatement.getRHS();
  }

  public boolean isPhiFunction() {
    return destinationStatement.getRHS() instanceof PhiFunction;
  }

  public PhiFunction getPhiFunction() {
    return (PhiFunction) getDestination();
  }
}
