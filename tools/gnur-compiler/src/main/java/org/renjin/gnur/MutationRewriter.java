/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur;

import org.renjin.gcc.analysis.FunctionBodyTransformer;
import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.gcc.logging.LogManager;

public class MutationRewriter implements FunctionBodyTransformer {

  private final ApiOracle apiOracle = new ApiOracle();

  @Override
  public boolean transform(LogManager logManager, GimpleCompilationUnit unit, GimpleFunction fn) {
    for (GimpleBasicBlock basicBlock : fn.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement instanceof GimpleCall) {
          GimpleCall call = (GimpleCall) statement;
          if(apiOracle.isPotentialMutator(call.getFunctionName())) {
            if(call.getLhs() == null) {
              System.err.println("WARNING: Call to " + call.getFunctionName() + " discards result in "
                  + fn.getName() + " at " + call.getSourceFile() + ":" + call.getLineNumber());
            }
          }
        }
      }
    }
    return false;
  }
}
