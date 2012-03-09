package org.renjin.primitives.io;

import java.io.IOException;
import java.io.PrintWriter;

import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.io.connections.Connection;

import r.lang.AtomicVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.Raw;
import r.lang.RawVector;
import r.lang.SEXP;
import r.lang.SexpVisitor;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class Cat extends SexpVisitor<String> {

  @Primitive
  public static void cat(ListVector list, Connection connection, String sep,
      SEXP fill, SEXP labels, boolean append) throws IOException {
    
    PrintWriter printWriter = connection.getPrintWriter();
    Cat visitor = new Cat(printWriter, sep, 0);
    for (SEXP element : list) {
      element.accept(visitor);
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

  public void visit(Raw raw) {
    catVector(new RawVector(raw));
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new EvalException(
        "argument of type '%s' cannot be handled by 'cat'", exp.getTypeName());
  }

}
