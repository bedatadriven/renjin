/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
  boolean isPure();

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
