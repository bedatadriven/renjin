package org.renjin.utils;

import org.renjin.sexp.IntVector;

import java.io.PrintWriter;

public class IntPrinter implements ColumnPrinter {
  
  private PrintWriter writer;
  private IntVector vector;
  private String naSymbol;

  public IntPrinter(PrintWriter writer, IntVector vector, String naSymbol) {
    this.writer = writer;
    this.vector = vector;
    this.naSymbol = naSymbol;
  }

  @Override
  public void print(int index) {
    int value = vector.getElementAsInt(index);
    if(IntVector.isNA(value)) {
      writer.write(naSymbol);
    } else {
      writer.print(value);
    }
  }
}
