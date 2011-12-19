package r.compiler.ir.tree;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class GetElementNode implements TreeNode {

  private List<TreeNode> children;
  
  public GetElementNode(TreeNode vector, TreeNode index) {
    super();
    children = Lists.newArrayList(vector, index);
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }
  
  public TreeNode getVector() {
    return children.get(0);
  }
  
  public TreeNode getIndex() {
    return children.get(1);
  }
  
  @Override
  public String toString() {
    return getVector() + "[" + getIndex() + "]";
  }

  @Override
  public void replace(int childIndex, TreeNode child) {
    children.set(childIndex, child);
  }

}
