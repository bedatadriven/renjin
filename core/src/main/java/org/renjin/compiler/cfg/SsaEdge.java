/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
