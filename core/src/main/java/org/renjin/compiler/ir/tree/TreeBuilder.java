package org.renjin.compiler.ir.tree;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.ir.tac.expressions.DynamicCall;
import org.renjin.compiler.ir.tac.expressions.ElementAccess;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.LValue;
import org.renjin.compiler.ir.tac.expressions.MakeClosure;
import org.renjin.compiler.ir.tac.expressions.PrimitiveCall;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.compiler.ir.tac.expressions.Temp;
import org.renjin.compiler.ir.tac.expressions.Variable;
import org.renjin.compiler.ir.tac.statements.Assignment;
import org.renjin.compiler.ir.tac.statements.Statement;



import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TreeBuilder {

  private List<Statement> trees = Lists.newArrayList();
  private Map<LValue, Assignment> definition = Maps.newHashMap();
  private Map<LValue, Integer> uses = Maps.newHashMap();
  private Set<LValue> usedOnce = Sets.newHashSet();
  private Set<Assignment> embeded = Sets.newHashSet();

  
  public static List<Statement> build(BasicBlock block) {
    return new TreeBuilder().doBuild(block);
  }
  
  private List<Statement> doBuild(BasicBlock block) {
    for(Statement stmt : block.getStatements()) {
      checkUses(stmt.getRHS());
      if(stmt instanceof Assignment) {
        Assignment assn = (Assignment) stmt;
        definition.put(assn.getLHS(), assn);
      }
      trees.add(stmt);
    }

    // variables that are only used once can be stored on the stack
    for(Map.Entry<LValue, Integer> entry : uses.entrySet()) {
      if(entry.getValue() == 1) {
        usedOnce.add(entry.getKey());
      }
    }


    System.out.println("used once = " + usedOnce);



    for(Statement stmt : block.getStatements()) {
      embed(stmt);
    }


    trees.removeAll(embeded);
        
    return trees;
  }


  private void embed(TreeNode treeNode) {
    List<Expression> children = treeNode.getChildren();
    for(int i=0;i!=children.size();++i) {
      Expression child = children.get(i);
      if(usedOnce.contains(child)) {
        Assignment assignment = definition.get(child);
        if(assignment != null) {
          treeNode.setChild(i, assignment.getRHS());
          embeded.add(assignment);
        }
      } else {
        embed(child);
      }
    }
  }


  private void checkUses(Expression expr) {
    if(expr instanceof Temp) {
      incrementUseCount((LValue) expr);
    }
    for(Expression child : expr.getChildren()) {
      checkUses(child);
    }
  }

  private void incrementUseCount(LValue lvalue) {
    Integer count = uses.get(lvalue);
    if(count == null) {
      uses.put(lvalue, 1);
    } else {
      uses.put(lvalue, count+1);
    }
  }

  private int getUseCount(LValue lvalue) {
    Integer count = uses.get(lvalue);
    return count == null ? 0 : count;
  }

}
