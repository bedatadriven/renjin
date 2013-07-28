package org.renjin.compiler.ir.tac.expressions;

import java.util.Set;

import org.renjin.compiler.ir.tree.TreeNode;
import org.renjin.eval.Context;
import org.renjin.sexp.SEXP;


public interface Expression extends TreeNode {


  /**
   * 
   * @return the set of all {@code Variable}s referenced by this {@code Expression}
   */
  Set<Variable> variables();

  /**
   * Recursively replaces all references to {@code variable} with 
   * {@code newVariable} in this {@code Expression}
   */
  Expression replaceVariable(Variable variable, Variable newVariable);


}
