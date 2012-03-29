package org.renjin.compiler.ir.tac.statements;

import java.util.Set;

import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tree.TreeNode;
import org.renjin.eval.Context;


public interface Statement extends TreeNode {
 
  Object interpret(Context context, Object temp[]);
  
  Iterable<IRLabel> possibleTargets();
  
  Set<Variable> variables();
    
  Expression getRHS();

  Statement withRHS(Expression newRHS);

  void accept(StatementVisitor visitor);
}