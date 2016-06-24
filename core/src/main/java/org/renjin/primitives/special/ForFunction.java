/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.special;

import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.cfg.DominanceTree;
import org.renjin.compiler.emit.ByteCodeEmitter;
import org.renjin.compiler.ir.ssa.SsaTransformer;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;


public class ForFunction extends SpecialFunction {

  public ForFunction() {
    super("for");
  }
  
  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList _args_unused) {
    
    PairList args = call.getArguments();
    Symbol symbol = args.getElementAsSEXP(0);
    SEXP elementsExp = context.evaluate(args.getElementAsSEXP(1), rho);
    if(!(elementsExp instanceof Vector)) {
      throw new EvalException("invalid for() loop sequence");
    }
    Vector elements = (Vector) elementsExp;
    SEXP statement = args.getElementAsSEXP(2);
    
    if(elements.length() > 200) {
      try {
        compileAndRun(context, rho, call);
      } catch (Exception e) {
        throw new EvalException(e);
      }
    } else {

      // Interpret the loop
      for (int i = 0; i != elements.length(); ++i) {
        try {
          rho.setVariable(symbol, elements.getElementAsSEXP(i));
          context.evaluate(statement, rho);
        } catch (BreakException e) {
          break;
        } catch (NextException e) {
          // next iteration
        }
      }
    }

    context.setInvisibleFlag();
    return Null.INSTANCE;
  }

  private void compileAndRun(Context context, Environment rho, FunctionCall call) throws IllegalAccessException, InstantiationException {
    IRBodyBuilder builder = new IRBodyBuilder(context, rho);
    IRBody body = builder.build(call);
    System.out.println(body);

    ControlFlowGraph cfg = new ControlFlowGraph(body);
    
    DominanceTree dTree = new DominanceTree(cfg);
    SsaTransformer ssaTransformer = new SsaTransformer(cfg, dTree);
    ssaTransformer.transform();

    System.out.println(cfg);

    VariableMap variableMap = new VariableMap(cfg);
    variableMap.resolveTypes();
    
    ssaTransformer.removePhiFunctions(variableMap);

    System.out.println(cfg);

    ByteCodeEmitter emitter = new ByteCodeEmitter(cfg);
    CompiledBody compiledBody = emitter.compile().newInstance();

    compiledBody.evaluate(context, rho);

  }
}
