/**
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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;


public class IRArgument {

  private String name;
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

  public IRArgument(Expression expression) {
    this.name = null;
    this.expression = expression;
  }
  
  public boolean isNamed() {
    return name != null;
  }

  public String getName() {
    return name;
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }
  
  public static boolean anyNamed(Iterable<IRArgument> arguments) {
    for (IRArgument argument : arguments) {
      if(argument.isNamed()) {
        return true;
      }
    }
    return false;
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
