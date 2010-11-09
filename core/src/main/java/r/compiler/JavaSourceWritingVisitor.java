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

package r.compiler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.commons.math.complex.Complex;
import r.lang.*;
import r.parser.ParseUtil;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.transform;

/**
 * Write
 */
public class JavaSourceWritingVisitor extends SexpVisitor<String> {

  private StringBuilder body = new StringBuilder();

  private int indent = 0;
  //private final SymbolMap symbolMap = new SymbolMap();

  // we put each complete expression in a seperate method
  // to avoid hitting the 64kb limit per method
  private List<String> methods = new ArrayList<String>();

  public void markSourceFile(String absolutePath) {
    body.append("\n\n    /** Source: ").append(absolutePath).append("*/");
  }

  /**
   * Writes out the source for a list
   * of expressions (Language Vector)
   *
   */
  @Override
  public void visit(ExpExp expressionVector) {
    for(SEXP exp : expressionVector) {
      String methodName = "eval" + methods.size();
      methods.add(methodName);
      body.append("\n\n  private void ").append(methodName).append("(SymbolTable symbols, EnvExp rho) {");

      exp.accept(this);
      body.append(".evaluate(rho);\n");

      body.append("  }\n");
    }
  }

  @Override
  public void visit(LangExp langExp) {
    body.append("\n    ");
    for(int i=0;i!=indent;++i) {
      body.append(" ");
    }
    indent++;
    body.append("call(");
    langExp.getFunction().accept(this);
    body.append(", ");
    langExp.getArguments().accept(this);
    body.append(")");
    indent--;
  }

  @Override
  public void visit(PairListExp listExp) {

    // if(any(listExp.listNodes(), ListExp.Predicates.hasTag())) {

    // throw new UnsupportedOperationException("tags not yet supported");
    //} else {
    body.append("list(");
    boolean needsComma = false;
    for(SEXP exp : listExp) {
      if(needsComma) {
        body.append(", ");
      } else {
        needsComma = true;
      }
      exp.accept(this);
    }
    body.append(")");
    //}
  }

  @Override
  public void visit(NullExp nullExp) {
    body.append("NULL");
  }

  @Override
  public void visit(SymbolExp symbolExp) {
    if(symbolExp == SymbolExp.MISSING_ARG) {
      body.append("MISSING");

    } else if(symbolExp == SymbolExp.UNBOUND_VALUE) {
      body.append("SymbolExp.UNBOUND_VALUE");

    } else {
      body.append("symbols.install(").append(
          ParseUtil.formatStringLiteral(symbolExp.getPrintName(), "NA"))
          .append(")");
    }
  }

  @Override
  public void visit(DoubleExp realExp) {
    body.append("c(");
    Joiner.on(", ").appendTo(body, transform(realExp, new ParseUtil.RealDeparser()));
    body.append(")");
  }

  public void visit(StringExp stringExp) {
    body.append("c(");
    Joiner.on(", ").appendTo(body, transform(stringExp, new ParseUtil.StringDeparser()));
    body.append(")");
  }

  @Override
  public void visit(LogicalExp logicalExp) {
    body.append("c(");
    Joiner.on(",").appendTo(body, transform(logicalExp, new ParseUtil.LogicalDeparser()));
    body.append(")");
  }

  @Override
  public void visit(IntExp intExp) {
    body.append("c_int(");
    Joiner.on(",").appendTo(body, transform(intExp, new ParseUtil.IntDeparser()));
    body.append(")");
  }

  @Override
  public void visit(ComplexExp complexExp) {
    body.append("c(");
    Joiner.on(",").appendTo(body, transform(complexExp, new ComplexWriter()));
    body.append(")");
  }

  @Override
  public void visit(ClosureExp closureExp) {
    body.append("new ClosureExp(this, ");
    closureExp.getFormals().accept(this);
    body.append(",");
    closureExp.getBody().accept(this);
    body.append(")");
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new UnsupportedOperationException("Unexpected SEXP of type " + exp.getClass() + " with value " +
    exp.toString() + "; the JavaSourceWritingVisitor can only generate code for the results of parse(), " +
        "code generation for evaled expressions is not supported.");
  }

  @VisibleForTesting
  String getBody() {
    return body.toString();
  }

  private class ComplexWriter implements Function<Complex, String> {
    @Override
    public String apply(Complex input) {
      return "new Complex(" + input.getReal() + ", " + input.getImaginary() + ")";
    }
  }

}
