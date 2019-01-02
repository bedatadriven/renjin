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
package org.renjin.gcc.analysis;

import org.renjin.gcc.logging.LogManager;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleFunctionRef;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.ListIterator;
import java.util.Set;

/**
 * Removes NOOP function calls
 */
public class FunctionCallPruner implements FunctionBodyTransformer {

  public static final FunctionCallPruner INSTANCE = new FunctionCallPruner();
  
  private Set<String> noops = Sets.newHashSet();
  
  public FunctionCallPruner() {
    noops.add("__builtin_stack_save");
    noops.add("__builtin_stack_restore");
  }

  @Override
  public boolean transform(LogManager logManager, GimpleCompilationUnit unit, GimpleFunction fn) {
    
    boolean updated = false;
    
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      ListIterator<GimpleStatement> it = basicBlock.getStatements().listIterator();
      while(it.hasNext()) {
        GimpleStatement statement = it.next();
        if(isNoop(statement)) {
          it.remove();
          updated = true;
        }
      }
    }
    
    return updated;
  }

  private boolean isNoop(GimpleStatement statement) {
    if(statement instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) statement;
      if (call.getFunction() instanceof GimpleAddressOf) {
        GimpleAddressOf addressOf = (GimpleAddressOf) call.getFunction();
        if(addressOf.getValue() instanceof GimpleFunctionRef) {
          GimpleFunctionRef ref = (GimpleFunctionRef) addressOf.getValue();
          return noops.contains(ref.getName());
        }
      }
    }
    return false;
  }
}
