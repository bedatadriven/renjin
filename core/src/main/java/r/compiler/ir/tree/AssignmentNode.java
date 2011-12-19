package r.compiler.ir.tree;

import java.util.Collections;
import java.util.List;

import r.compiler.ir.IRUtils;
import r.compiler.ir.tac.IRLabel;
import r.compiler.ir.tac.expressions.LValue;

public class AssignmentNode implements TreeNode {

  private LValue lhs;
  private TreeNode rhs;
  
  public AssignmentNode(LValue lValue, TreeNode rhs) {
    super();
    this.lhs = lValue;
    this.rhs = rhs;
  }

  @Override
  public List<TreeNode> getChildren() {
    return Collections.singletonList(rhs);
  }
  
  public TreeNode getRHS() {
    return rhs;
  }

  @Override
  public String toString() {
    return lhs + " " + IRUtils.LEFT_ARROW + " " + rhs;
  }

  @Override
  public void replace(int childIndex, TreeNode child) {
    throw new UnsupportedOperationException();
  }
  
}
