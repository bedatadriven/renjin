package org.renjin.compiler.ir.tree;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.PhiFunction;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;

import java.util.List;
import java.util.Map;

public class TreeBuilder {

  private ControlFlowGraph cfg;
  private VariableMap variableMap;

  public TreeBuilder(ControlFlowGraph cfg, VariableMap variableMap) {
    this.cfg = cfg;
    this.variableMap = variableMap;
  }

  public List<Statement> build(BasicBlock bb) {

    Map<LValue, Expression> definitions = Maps.newHashMap();
    List<Statement> statements = Lists.newArrayList();

    for(Statement statement : bb.getStatements()) {

      if(isPhiAssignment(statement)) {
        // NO OP

      } else if(isSyncPoint(bb, statement)) {
        statements.add(buildUp(statement, definitions));

      } else if(statement instanceof Assignment) {
        definitions.put(((Assignment) statement).getLHS(), statement.getRHS());

      } else {
        // NO OP - pure expression with no side effects
      }
    }
    return statements;
  }

  private boolean isPhiAssignment(Statement statement) {
    return statement instanceof Assignment &&
        statement.getRHS() instanceof PhiFunction;
  }

  private <T extends TreeNode> T buildUp(T parent, Map<LValue, Expression> definitions) {
    for(int i=0;i!=parent.getChildCount();++i) {
      if(definitions.containsKey(parent.childAt(i))) {
        parent.setChild(i, buildUp(definitions.get(i), definitions));
      }
    }
    return parent;
  }


  /**
   * at "sync points" within the basic block, we have to emit the instruction
   * because it has side effects. We can freely arrange statements that are not
   * considered to be sync points
   */
  private boolean isSyncPoint(BasicBlock bb, Statement statement) {
    if(statement instanceof Expression) {
      return !statement.getRHS().isDefinitelyPure();
    } else if(statement instanceof Assignment) {
      Assignment assignment = (Assignment)statement;
      if(!assignment.getRHS().isDefinitelyPure()) {
        return true;
      }
      // if the value of this expression is used in other
      // basic blocks, then we have to assign it to a local variable
      LValue lhs = assignment.getLHS();
      if(variableMap.isUsedOutsideOf(lhs, bb)) {
        return true;
      }

      // otherwise, we should be free to rearrange within this BB
      return false;


    } else {
      // any other statements, like goto, return, etc, definitely
      // have side effects so we have to stop and emit
      return true;
    }

  }

}
