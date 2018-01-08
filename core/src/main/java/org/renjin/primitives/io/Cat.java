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
package org.renjin.primitives.io;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.PrintWriter;


public class Cat extends SexpVisitor<String> {

  @Internal
  @Invisible
  public static void cat(@Current Context context, ListVector list, SEXP connection, String sep,
      int fill, SEXP labels, boolean append) throws IOException {


    // Materialize all the elements in parallel if neccessary
    list = context.materialize(list);

    PrintWriter printWriter = Connections.getConnection(context, connection).getPrintWriter();
    Cat visitor = new Cat(printWriter, sep, 0);
    for (SEXP element : list) {
      element.accept(visitor);
      if(fill > 0) {
        printWriter.println();
      }
    }
    
    // The GNU R implementation appears to treat a newline separator as a special case:
    // Any other string is only printed between elements, but if sep contains a newline character,
    // a final, trailing, newline is also printed
    if(sep.contains("\n")) {
      printWriter.println();
    }
    printWriter.flush();
  }
  
  
  private final PrintWriter writer;
  private String separator;
  private boolean needsSeparator = false;
  private int fill;

  private Cat(PrintWriter writer, String separator, int fill) {
    this.writer = writer;
    this.separator = separator;
    this.fill = fill;
  }

  @Override
  public void visit(StringVector vector) {
    catVector(vector);
  }

  @Override
  public void visit(IntVector vector) {
    catVector(vector);
  }

  @Override
  public void visit(LogicalVector vector) {
    catVector(vector);
  }

  @Override
  public void visit(Null nullExpression) {
    // do nothing
  }

  @Override
  public void visit(DoubleVector vector) {
    catVector(vector);
  }

  private void catVector(AtomicVector vector) {
    for (int i = 0; i != vector.length(); ++i) {
      catElement(vector.getElementAsString(i));
    }
  }

  @Override
  public void visit(Symbol symbol) {
    catElement(symbol.getPrintName());
  }

  private void catElement(String element) {
    if (needsSeparator) {
      writer.print(separator);
    } else {
      needsSeparator = true;
    }
    writer.print(element);
  }

  @Override
  public void visit(RawVector vector) {
    catVector(vector);
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new EvalException(
        "argument of type '%s' cannot be handled by 'cat'", exp.getTypeName());
  }

}
