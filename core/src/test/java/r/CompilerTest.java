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

package r;

import org.junit.Ignore;
import org.junit.Test;
import r.lang.*;
import r.parser.RParser;

import java.io.FileReader;
import java.io.IOException;

@Ignore("work in progress")
public class CompilerTest {


  @Test
  public void test() throws IOException {
    Context context = Context.newTopLevelContext();

    Environment rho = Environment.createChildEnvironment(context.getEnvironment());

    ExpressionVector source = RParser.parseSource(new FileReader("core/src/main/r/base/factor.R"));
    source.evaluate(context, rho);

    for(Symbol symbol : rho.getSymbolNames()) {
      SEXP value = rho.getVariable(symbol);
      System.out.println(symbol.getPrintName() + " = " + value.getClass().getSimpleName());

      if(value instanceof Closure) {
        Closure cl = (Closure) value;
        System.out.println("CLOSURE");
        cl.getBody().accept(new DebugVisitor());
      }
    }

  }

  private class DebugVisitor extends SexpVisitor {
    private int indent = 1;
    @Override
    public void visit(FunctionCall call) {
      println(call.getFunction().toString());
      indent++;
      call.getArguments().accept(this);
      indent--;
    }

    @Override
    protected void unhandled(SEXP exp) {
      println(exp.toString());
    }

    void println(String s) {
      for(int i=0;i!=indent;++i) {
        System.out.print("    ");
      }
      System.out.println(s);
    }


  }
}

