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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.List;


public class IRArgument {

  private String name;
  private SEXP sexp;
  private Expression expression;

  public IRArgument(String name, Expression expression) {
    this.name = name;
    this.expression = expression;
  }


  public IRArgument(SEXP name, SimpleExpression expression) {
    if(name == Null.INSTANCE) {
      this.name = null;
    } else if(name instanceof Symbol) {
      this.name = ((Symbol) name).getPrintName();
    } else {
      throw new IllegalArgumentException("name: " + name);
    }
    this.expression = expression;
  }

  public IRArgument(PairList.Node argument, SimpleExpression expression) {
    this(argument.getRawTag(), expression);
    this.sexp = argument.getValue();
  }

  public IRArgument(Expression expression) {
    this.name = null;
    this.expression = expression;
  }

  public IRArgument(String name, Expression expression, SEXP sexp) {
    this.name = name;
    this.expression = expression;
    this.sexp = sexp;
  }

  public boolean isNamed() {
    return name != null;
  }

  public String getName() {
    return name;
  }

  public SEXP getSexp() {
    return sexp;
  }

  public Expression getExpression() {
    return expression;
  }

  public IRArgument withExpression(Expression expression) {
    if(this.expression == expression) {
      return this;
    } else {
      return new IRArgument(name, expression, sexp);
    }
  }

  public static String[] names(List<IRArgument> arguments) {
    String[] names = new String[arguments.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = arguments.get(i).getName();
    }
    return names;
  }

  @Override
  public String toString() {
    if(isNamed()) {
      return name + " = " + expression;
    } else {
      return expression.toString();
    }
  }
}
