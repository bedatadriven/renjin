package r.compiler.ir.tree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import r.compiler.cfg.BasicBlock;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.ElementAccess;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.LValue;
import r.compiler.ir.tac.expressions.MakeClosure;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.expressions.SimpleExpression;
import r.compiler.ir.tac.statements.Assignment;
import r.compiler.ir.tac.statements.Statement;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TreeBuilder {

  private List<TreeNode> trees = Lists.newArrayList();
  private Map<LValue, AssignmentNode> definition = Maps.newHashMap();
  private Map<LValue, Integer> uses = Maps.newHashMap();
  private Set<LValue> usedOnce = Sets.newHashSet();
  private Set<AssignmentNode> embeded = Sets.newHashSet();
  
  public List<TreeNode> build(BasicBlock block) {
    for(Statement stmt : block.getStatements()) {
      if(stmt instanceof Assignment) {
        addAssignment( (Assignment)stmt );
      } 
    }
    
    // variables that are only used once can be stored on the stack
    for(Map.Entry<LValue, Integer> entry : uses.entrySet()) {
      if(entry.getValue() == 1) {
        usedOnce.add(entry.getKey());
      }
    }
    
    System.out.println("used once = " + usedOnce);
    
    // embed variables that are used once in the tree where they are used
    for(int i=1;i!=trees.size();++i) {
      embed(trees.get(i));
    }
    trees.removeAll(embeded);
    
    return trees;
  }
  

  private void embed(TreeNode treeNode) {
    List<TreeNode> children = treeNode.getChildren();
    for(int i=0;i!=children.size();++i) {
      TreeNode child = children.get(i);
      if(child instanceof ValueNode) {
        ValueNode valueNode = (ValueNode) child;
        if(usedOnce.contains(valueNode.getValue())) {
          AssignmentNode assignment = definition.get(valueNode.getValue());
          if(assignment != null) {
            treeNode.replace(i, assignment.getRHS());
            embeded.add(assignment);
          }
        }
      } else {
        embed(child);
      }
    }
  }
  


  private void addAssignment(Assignment stmt) {
    AssignmentNode node = new AssignmentNode(stmt.getLHS(), createNode(stmt.getRHS()));
    trees.add(node);
    definition.put(stmt.getLHS(), node);
  }

  private TreeNode createNode(Expression expr) {
     if(expr instanceof PrimitiveCall) {
       return createPrimitiveCallNode((PrimitiveCall)expr);
     } else if(expr instanceof DynamicCall) {
       return createDynamicCallNode((DynamicCall)expr);
     } else if(expr instanceof ElementAccess) {
       return createGetElementNode((ElementAccess) expr);
     } else if(expr instanceof SimpleExpression) {
       return createValueNode((SimpleExpression) expr);
     } else if(expr instanceof MakeClosure) {
       return new ClosureNode( ((MakeClosure)expr).getFunction() );
     } else {
       throw new UnsupportedOperationException(expr.toString());
     }
  }

  private ValueNode createValueNode(SimpleExpression expr) {
    if(expr instanceof LValue) {
      LValue lvalue = (LValue)expr;
      Integer count = uses.get(expr);
      if(count == null) {
        uses.put(lvalue, 1);
      } else {
        uses.put(lvalue, count+1);
      }
    }
    return new ValueNode((SimpleExpression)expr);
  }

  private TreeNode createGetElementNode(ElementAccess expr) {
    return new GetElementNode(createNode(expr.getVector()), createNode(expr.getIndex()));
  }

  private TreeNode createPrimitiveCallNode(PrimitiveCall expr) {
    return new PrimitiveCallNode(expr.getName(), createNodeList(expr.getArguments()));
  }
  
  private TreeNode createDynamicCallNode(DynamicCall expr) {
    return new DynamicCallNode(createNode(expr.getName()), createNodeList(expr.getArguments()));
  }

  private List<TreeNode> createNodeList(List<Expression> arguments) {
    List<TreeNode> nodes = Lists.newArrayList();
    for(Expression expr : arguments) {
      nodes.add(createNode(expr));
    }
    return nodes;
  }
}
