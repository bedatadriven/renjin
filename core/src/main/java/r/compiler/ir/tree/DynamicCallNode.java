package r.compiler.ir.tree;

import java.util.List;

import r.lang.Symbol;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DynamicCallNode implements TreeNode {
  private List<TreeNode> children;
  
  public DynamicCallNode(TreeNode function, List<TreeNode> arguments) {
    super();
    children = Lists.newArrayList();
    children.add(function);
    children.addAll(arguments);
  }

  @Override
  public List<TreeNode> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    return "dynamic<" + getFunction() +">(" + Joiner.on(", ").join(getArguments()) + ")";
  }
  
  public TreeNode getFunction() {
    return children.get(0);
  }
 
  public List<TreeNode> getArguments() {
    return children.subList(1, children.size());
  }

  @Override
  public void replace(int childIndex, TreeNode child) {
    children.set(childIndex, child);
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.dynamicCall(this);
  } 
}
