package r.compiler.ir.tree;

import java.util.Collections;
import java.util.List;

import r.compiler.ir.tac.IRFunction;

public class ClosureNode implements TreeNode {
  private IRFunction function;

  public ClosureNode(IRFunction function) {
    super();
    this.function = function;
  }

  @Override
  public List<TreeNode> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public void replace(int childIndex, TreeNode child) {
    
  }
  
  @Override
  public String toString() {
    return "closure(" + function + ")";
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.closure(this);
  }
}
