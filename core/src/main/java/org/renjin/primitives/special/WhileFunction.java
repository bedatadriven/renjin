/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.special;

import org.renjin.compiler.Compiler;
import org.renjin.eval.Context;
import org.renjin.sexp.*;

public class WhileFunction extends SpecialFunction {

  public WhileFunction() {
    super("while");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    SEXP condition = args.getElementAsSEXP(0);
    SEXP statement = args.getElementAsSEXP(1);
    
    int iterationCount = 0;
    boolean compilationFailed = false;

    while(asLogicalNoNA(context, call, context.evaluate( condition, rho))) {

      try {
        iterationCount ++;

        if(iterationCount > 50 && !compilationFailed) {
          if(Compiler.tryCompileAndRun(context, rho, call)) {
            break;
          } else {
            compilationFailed = false;
          }
        }
        
        context.evaluate( statement, rho);
        
      } catch(BreakException e) {
        break;
      } catch(NextException e) {
        // next loop iteration
      }
    }
//    System.out.println("While count: " + iterationCount);
    context.setInvisibleFlag();
    return Null.INSTANCE;
  }
}
