package org.renjin.utils;

import org.renjin.sexp.StringVector;

import java.io.PrintWriter;


public class StringPrinter implements ColumnPrinter {

  private PrintWriter writer;
  private StringVector vector;
  private boolean quote;
  private String naSymbol;

  public StringPrinter(PrintWriter writer, StringVector vector, boolean quote, String naSymbol) {
    this.writer = writer;
    this.vector = vector;
    this.quote = quote;
    this.naSymbol = naSymbol;
  }

  @Override
  public void print(int index) {
    String value = vector.getElementAsString(index);
    if(value == null) {
      writer.write(naSymbol);
    } else {
      if(quote) {
        writer.write('"');
      }
      writer.write(value);
      if(quote) {
        writer.write('"');
      }
    }
  }
}
