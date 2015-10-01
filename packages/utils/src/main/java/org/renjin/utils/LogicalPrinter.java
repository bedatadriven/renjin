package org.renjin.utils;

import org.renjin.sexp.IntVector;
import org.renjin.sexp.LogicalVector;

import java.io.PrintWriter;


public class LogicalPrinter implements ColumnPrinter {
  private PrintWriter writer;
  private LogicalVector vector;
  private String naSymbol;

  public LogicalPrinter(PrintWriter writer, LogicalVector vector, String naSymbol) {
    this.writer = writer;
    this.vector = vector;
    this.naSymbol = naSymbol;
  }

  @Override
  public void print(int index) {
    int value = vector.getElementAsRawLogical(index);
    if(IntVector.isNA(value)) {
      writer.write(naSymbol);
    } else if(value == 0) {
      writer.write("FALSE");
    } else {
      writer.write("TRUE");
    }
  }
}
