/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;

import java.io.PrintWriter;


public class FactorPrinter implements ColumnPrinter {
  
  private PrintWriter writer;
  private IntVector vector;
  private String naSymbol;
  private String[] levels;

  public FactorPrinter(PrintWriter writer, IntVector vector, boolean quote, String naSymbol) {
    this.writer = writer;
    this.vector = vector;
    this.naSymbol = naSymbol;
    this.levels = formatLevels(vector, quote);
  }

  private String[] formatLevels(IntVector vector, boolean quote) {
    SEXP attribute = vector.getAttribute(Symbols.LEVELS);
    if(!(attribute instanceof AtomicVector)) {
      throw new EvalException("Expected 'levels' attribute of type character");
    }
    AtomicVector levelsVector = (AtomicVector) attribute;
    String[] levels = new String[levelsVector.length()];
    for(int i=0;i!=levelsVector.length();++i) {
      if(quote) {
        levels[i] = "\"" + levelsVector.getElementAsString(i) + "\"";
      } else {
        levels[i] = levelsVector.getElementAsString(i);
      }
    }
    return levels;
  }


  @Override
  public void print(int index) {
    int valueIndex = vector.getElementAsInt(index);
    if (IntVector.isNA(valueIndex) || valueIndex > levels.length) {
      writer.write(naSymbol);
    } else {
      writer.write(levels[valueIndex-1]);
    }
  }
}
