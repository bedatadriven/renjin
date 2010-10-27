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
import com.google.common.base.Joiner;
import r.lang.*;
import r.parser.ParseUtil;

import java.io.PrintStream;

import static com.google.common.collect.Iterables.transform;

/**
 * Write
 */
public class JavaSourceWritingVisitor extends SexpVisitor<String> {

  private StringBuilder body = new StringBuilder();

  private int indent = 0;
  private final SymbolMap symbolMap = new SymbolMap();

  /**
   * Writes out the source for a list
   * of expressions (Language Vector)
   *
   */
  @Override
  public void visit(ExpExp expressionVector) {
    for(SEXP exp : expressionVector) {
      exp.accept(this);
      body.append(".evaluate(rho);\n");
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
  public void visit(ListExp listExp) {

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
  public void visit(NilExp nilExp) {
    body.append("NULL");
  }

  @Override
  public void visit(SymbolExp symbolExp) {
    if(symbolExp == SymbolExp.MISSING_ARG) {
      body.append("MISSING");

    } else if(symbolExp == SymbolExp.UNBOUND_VALUE) {
      body.append("SymbolExp.UNBOUND_VALUE");

    } else {
      body.append(symbolMap.getSymbolName(symbolExp));
    }
  }

  @Override
  public void visit(RealExp realExp) {
    body.append("c(");
    Joiner.on(", ").appendTo(body, transform(realExp, new ParseUtil.RealDeparser()));
    body.append(")");
  }

  public void visit(StringExp stringExp) {
    body.append("c(");
    Joiner.on(", ").appendTo(body, transform(stringExp, new ParseUtil.StringDeparser()));
    body.append(")");
  }

  @VisibleForTesting
  String getBody() {
    return body.toString();
  }

  public void writeTo(String packageName, String className, PrintStream writer) {
    writer.println(String.format("package %s;", packageName));
    writer.println();
    writer.println("import r.lang.*;");
    writer.println("import r.compiler.runtime.AbstractProgram;");
    writer.println();
    writer.println("public class " + className + " extends AbstractProgram {");
    writer.println();
    writer.println("  @Override");
    writer.println("  public void evaluate(EnvExp rho) {");
    writer.println();
    writer.println(symbolMap.getSymbolDefinitions());

    writer.print(body.toString());
    writer.println("  }");
    writer.println("}");
  }
}
