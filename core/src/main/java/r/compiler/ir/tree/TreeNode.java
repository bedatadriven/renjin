package r.compiler.ir.tree;

import java.util.List;

import r.compiler.ir.tac.expressions.Expression;

public interface TreeNode {

  List<Expression> getChildren();

  void setChild(int childIndex, Expression child);
  
  
}
