package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.tac.TreeNode;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Map;


public interface Expression extends TreeNode {


  /**
   *
   * @return true if we are absolutely certain this expression has no side effects
   */
  boolean isDefinitelyPure();

  /**
   * Emits the JVM byte code to push the value of this expression on the stack
   *
   * @param emitContext
   * @param mv
   * @return the number of items pushed onto the stack
   */
  int load(EmitContext emitContext, InstructionAdapter mv);


  Type getType();

  /**
   * Resolves and stores the type of this Expression, based on it's
   * child nodes
   * @param typeMap
   */
  ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap);

  ValueBounds getValueBounds();
  
}
