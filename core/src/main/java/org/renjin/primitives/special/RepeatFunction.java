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
package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

public class RepeatFunction extends SpecialFunction {

  public RepeatFunction() {
    super("repeat");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {
    SEXP statement = args.getElementAsSEXP(0);

//    int iterationCount = 0;
//    boolean compilationFailed = false;
    
    while(true) {
      try {
        
//        if(iterationCount > 50) {
//          if (LoopCompiler.tryCompileAndRun(context, rho, call)) {
//            break;
//          } else {
//            compilationFailed = true;
//          }
//        }
        context.evaluate( statement, rho);
//        iterationCount++;
      } catch(BreakException e) {
        break;
      } catch(NextException e) {
        // next loop iteration
      }
    }
    context.setInvisibleFlag();
    return Null.INSTANCE;
  }

}
