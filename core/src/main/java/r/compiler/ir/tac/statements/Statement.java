package r.compiler.ir.tac.statements;

import java.util.Set;

import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.Variable;
import r.compiler.ir.tree.TreeNode;
import r.lang.Context;

public interface Statement extends TreeNode {
 
  Object interpret(Context context, Object temp[]);
  
  Iterable<IRLabel> possibleTargets();
  
  Set<Variable> variables();
    
  Expression getRHS();

  Statement withRHS(Expression newRHS);

  void accept(StatementVisitor visitor);
}