package org.renjin.compiler.cfg;

import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

import com.google.common.base.Predicate;

public class CfgPredicates {
  private CfgPredicates() {}
  
  public static Predicate<BasicBlock> containsAssignmentTo(final Variable variable) {
    return new Predicate<BasicBlock>() {

      @Override
      public boolean apply(BasicBlock input) {
        for(Statement stmt : input.getStatements()) {
          if(stmt instanceof Assignment) {
            Assignment assignment = (Assignment) stmt;
            if(assignment.getLHS().equals(variable)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }
  
  public static Predicate<Statement> isPhiAssignment() {
    return new Predicate<Statement>() {

      @Override
      public boolean apply(Statement input) {
        if(!(input instanceof Assignment)) {
          return false;
        }
        Assignment assignment = (Assignment) input;
        return assignment.getRHS() instanceof PhiFunction;
      }
    };
  }
  
}
