package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.expressions.Expression;


public interface TreeNode {

  void setChild(int childIndex, Expression child);
  
  int getChildCount();

  Expression childAt(int index);
  
  
}
