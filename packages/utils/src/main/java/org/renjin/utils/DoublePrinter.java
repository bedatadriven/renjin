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
package org.renjin.utils;

import org.renjin.sexp.DoubleVector;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


public class DoublePrinter implements ColumnPrinter {

  private final PrintWriter writer;
  private final DoubleVector vector;
  private String naSymbol;
  private final DecimalFormat format;

  public DoublePrinter(PrintWriter writer, DoubleVector vector, String decimal, String naSymbol) {
    this.writer = writer;
    this.vector = vector;
    this.naSymbol = naSymbol;

    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator(decimal.charAt(0));
    
    this.format = new DecimalFormat();
    format.setGroupingUsed(false);
    format.setDecimalFormatSymbols(symbols);
    format.setMinimumIntegerDigits(0);
    format.setMaximumFractionDigits(6);
  }

  @Override
  public void print(int rowNumber) {
    double value = vector.getElementAsDouble(rowNumber);
    if(DoubleVector.isNA(value)) {
      writer.write(naSymbol);
    } else {
      writer.write(format.format(value));
    }
  }
}
