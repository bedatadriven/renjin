package r.compiler.ir.tree;

import java.util.List;

public interface TreeNode {

  List<TreeNode> getChildren();

  void replace(int childIndex, TreeNode child);
  
}
