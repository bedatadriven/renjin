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

package r.base;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import r.lang.*;
import r.parser.ParseUtil;

import static com.google.common.collect.Iterables.transform;

public class Parse {

  private Parse() {}

  public static String deparse(SEXP exp) {
    return new DeparsingVisitor(exp).getResult();
  }

  private static class DeparsingVisitor extends SexpVisitor<String> {

    private StringBuilder deparsed = new StringBuilder();

    public DeparsingVisitor(SEXP exp) {
      exp.accept(this);
    }

    @Override
    public void visit(CHARSEXP charExp) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visit(ComplexVector complexExp) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Environment environment) {
      // this is somewhat random; it's isn't parsable in any case
      deparsed.append("<environment>");
    }

    @Override
    public void visit(ExpressionVector vector) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visit(BuiltinFunction builtin) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visit(IntVector vector) {
      appendVector(vector, new ParseUtil.IntDeparser());
    }

    @Override
    public void visit(PairList.Node pairList) {
      deparsed.append("list(");
      boolean needsComma = false;
      for(SEXP sexp : pairList.values()) {
        if(needsComma) {
          deparsed.append(", ");
        } else {
          needsComma = true;
        }
        sexp.accept(this);
      }
      deparsed.append(")");
    }

    @Override
    public void visit(Null nullExpression) {
      deparsed.append("NULL");
    }

    @Override
    public void visit(PrimitiveFunction primitive) {
      super.visit(primitive);
    }

    @Override
    public void visit(Promise promise) {
      super.visit(promise);
    }

    @Override
    public void visit(DoubleVector vector) {
      appendVector(vector, new ParseUtil.RealDeparser());
    }

    @Override
    public void visit(StringVector vector) {
      appendVector(vector, new ParseUtil.StringDeparser());
    }

    @Override
    public void visit(LogicalVector vector) {
     appendVector(vector, new ParseUtil.LogicalDeparser());
    }

    @Override
    public void visit(FunctionCall call) {
      super.visit(call);
    }

    @Override
    public void visit(Symbol symbol) {
      deparsed.append(symbol.getPrintName());
    }

    @Override
    public void visit(Closure closure) {
      throw new UnsupportedOperationException("deparsing of closures not yet implemented");
    }

    @Override
    public String getResult() {
      return deparsed.toString();
    }

    public <T> void appendVector(Iterable<T> values, Function<T, String> deparser) {
      if(Iterables.size(values) == 1 ) {
        deparsed.append(deparser.apply(values.iterator().next()));
      } else {
        deparsed.append("c(");
        Joiner.on(", ").appendTo(deparsed, transform(values, deparser));
        deparsed.append(")");
      }
    }
  }
}
