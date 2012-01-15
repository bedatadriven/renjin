package r.compiler.ir.tree;

import java.util.Collections;
import java.util.List;

import r.compiler.ir.tac.expressions.SimpleExpression;

public class ValueNode implements TreeNode {
  private SimpleExpression expression;

  public ValueNode(SimpleExpression expression) {
    super();
    this.expression = expression;
  }
  
  public SimpleExpression getValue() {
    return expression;
  }

  @Override
  public List<TreeNode> getChildren() {
    return Collections.emptyList();
  }
  
  @Override
  public String toString() {
    return expression.toString();
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.value(this);
  }

  @Override
  public void replace(int childIndex, TreeNode child) {
    throw new UnsupportedOperationException();
  }
}
