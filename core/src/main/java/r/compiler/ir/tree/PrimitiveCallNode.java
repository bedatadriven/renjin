package r.compiler.ir.tree;

import java.util.List;

import r.lang.Symbol;

import com.google.common.base.Joiner;

public class PrimitiveCallNode implements TreeNode {
  private Symbol name;
  private List<TreeNode> arguments;
  
  public PrimitiveCallNode(Symbol name, List<TreeNode> arguments) {
    super();
    this.name = name;
    this.arguments = arguments;
  }

  @Override
  public List<TreeNode> getChildren() {
    return arguments;
  }

  @Override
  public String toString() {
    return "primitive<" + name +">(" + Joiner.on(", ").join(arguments) + ")";
  }

  @Override
  public void replace(int childIndex, TreeNode child) {
    arguments.set(childIndex, child);
  } 
  
  
}
