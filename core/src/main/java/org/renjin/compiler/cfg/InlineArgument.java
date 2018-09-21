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
package org.renjin.compiler.cfg;

import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.eval.HasName;
import org.renjin.sexp.SEXP;

import java.util.ArrayList;
import java.util.List;

public class InlineArgument implements HasName {
  private String name;
  private SEXP sexp;

  public InlineArgument() {
  }

  public InlineArgument(SEXP sexp) {
    this.sexp = sexp;
  }

  public InlineArgument(IRArgument argument) {
    this.name = argument.getName();
    this.sexp = argument.getSexp();
  }

  public InlineArgument(ArgumentBounds argument) {
    this.name = argument.getName();
    this.sexp = argument.getSexp();
  }

  public SEXP getSexp() {
    return sexp;
  }

  @Override
  public String getName() {
    return name;
  }

  public static List<InlineArgument> from(List<IRArgument> arguments) {
    List<InlineArgument> list = new ArrayList<>(arguments.size());
    for (IRArgument argument : arguments) {
      list.add(new InlineArgument(argument));
    }
    return list;
  }

  public static List<InlineArgument> fromBounds(List<ArgumentBounds> arguments) {
    List<InlineArgument> list = new ArrayList<>(arguments.size());
    for (ArgumentBounds argument : arguments) {
      list.add(new InlineArgument(argument));
    }
    return list;
  }
}
