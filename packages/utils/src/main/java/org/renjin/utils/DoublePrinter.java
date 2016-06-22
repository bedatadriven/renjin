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
