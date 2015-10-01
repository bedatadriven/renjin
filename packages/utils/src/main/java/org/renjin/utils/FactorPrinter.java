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
