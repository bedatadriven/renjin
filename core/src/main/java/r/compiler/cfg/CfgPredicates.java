package r.compiler.cfg;

import r.compiler.ir.ssa.PhiFunction;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.Statement;
import r.compiler.ir.tac.operand.Variable;

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
            if(assignment.getTarget().equals(variable)) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }
}
