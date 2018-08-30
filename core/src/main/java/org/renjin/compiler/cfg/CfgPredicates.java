/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.base.Predicate;

public class CfgPredicates {
  private CfgPredicates() {}
  
  public static Predicate<BasicBlock> containsAssignmentTo(final Variable variable) {
    return input -> {
      for(Statement stmt : input.getStatements()) {
        if(stmt instanceof Assignment) {
          Assignment assignment = (Assignment) stmt;
          if(assignment.getLHS().equals(variable)) {
            return true;
          }
        }
      }
      return false;
    };
  }

  public static Predicate<Statement> isPhiAssignment() {
    return input -> {
      if(!(input instanceof Assignment)) {
        return false;
      }
      Assignment assignment = (Assignment) input;
      return assignment.getRHS() instanceof PhiFunction;
    };
  }
  
}
