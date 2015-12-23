package org.renjin.gcc.analysis;

import com.google.common.annotations.VisibleForTesting;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.statement.GimpleStatement;

import java.util.HashSet;
import java.util.Set;

/**
 * Transforms the 3-address code into trees suitable for code generation
 * targeting the stack-based JVM
 */
public class TreeBuilder implements FunctionBodyTransformer {

  public static final TreeBuilder INSTANCE = new TreeBuilder();

  @Override
  public boolean transform(GimpleCompilationUnit unit, GimpleFunction fn) {

    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      buildTrees(fn, basicBlock);
    }

    return false;
  }

  @VisibleForTesting
  void buildTrees(GimpleFunction function, GimpleBasicBlock basicBlock) {

    if(!basicBlock.isEmpty()) {
      Set<Integer> variablesToNest = findUnnamedSingleUseVariable(function);

      StatementNode head = StatementNode.createLinkedList(basicBlock);
      StatementNode current = head.firstDefinition();
      
      while (current != null) {
        GimpleVariableRef var = current.getLhs();
        if (var != null && variablesToNest.contains(var.getId())) {
          if (current.hasSuccessor()) {
            // if the immediately following statement contains a reference
            // to this definition, then eliminate this variable and nest
            // its value into the following statement
            StatementNode successor = current.getSuccessor();
            if (successor.replace(var, current.nested())) {
              
              // remove the variable from the function
              function.removeVariable(var);

              // remove the definition from the basic block
              basicBlock.getInstructions().remove(current.getStatement());

              // remove this node and back up to the predecessor
              current = current.remove();
              continue;
            }
          }
        }
        // if we can't merge, move onto the next definition
        current = current.nextDefinition();
      }
    }
  }

  /**
   * Finds the ids of all unnamed, local variables that are used only once
   */
  Set<Integer> findUnnamedSingleUseVariable(GimpleFunction function) {
    Set<Integer> localVariables = new HashSet<>();
    Set<Integer> used = new HashSet<>();
    Set<Integer> usedOnce = new HashSet<>();

    for (GimpleVarDecl decl : function.getVariableDeclarations()) {
     // if(!decl.isNamed()) {
        localVariables.add(decl.getId());
     // }
    }

    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getInstructions()) {
        for (GimpleVariableRef ref : statement.findUses(GimpleVariableRef.class)) {
          if(localVariables.contains(ref.getId())) {
            boolean firstUse = used.add(ref.getId());
            if(firstUse) {
              usedOnce.add(ref.getId());
            } else {
              usedOnce.remove(ref.getId());
            }
          }
        }
      }
    }
    return usedOnce;
  }
}
