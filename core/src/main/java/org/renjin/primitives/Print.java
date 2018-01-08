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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Generic;
import org.renjin.invoke.annotations.Internal;
import org.renjin.parser.StringLiterals;
import org.renjin.primitives.print.*;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.io.IOException;
import java.util.List;

public class Print {

  private Print() {}

  @Internal("print.default")
  public static SEXP printDefault(@Current Context context, SEXP expression, SEXP digits, boolean quote, SEXP naPrint,
      SEXP printGap, SEXP right, SEXP max, SEXP useSource, SEXP noOp, boolean useS4) throws IOException {

    if(useS4 && Types.isS4(expression)) {
      printS4(context, expression);
    
    } else {

      // Side affect alert!
      // Trigger any deferred computation
      expression = context.materialize(expression);


      PrintingVisitor visitor = new PrintingVisitor(context)
          .setCharactersPerLine(80)
          .setQuote(quote);
      expression.accept(visitor);
      context.getSession().getStdOut().print(visitor.getResult());
    }

    context.getSession().getStdOut().flush();
    context.setInvisibleFlag();
    return expression;

  }

  private static void printS4(Context context, SEXP expression) {
    context.evaluate(FunctionCall.newCall(Symbol.get("show"), expression));
  }

  public static String doPrint(SEXP expression) {
    // we only need the context because we may need to force
    // an unevaluated promise... but this seems super unlikely...
    // try removing this once we have a larger test suite built up
    PrintingVisitor visitor = new PrintingVisitor(null)
        .setCharactersPerLine(80);
    expression.accept(visitor);

    return visitor.getResult();
  }

  @Generic
  @Internal("print.function")
  public static void printFunction(@Current Context context, SEXP x, boolean useSource, 
                                   @ArgumentList ListVector extraArguments) throws IOException {
    context.getSession().getStdOut().println(x.toString());
    context.getSession().getStdOut().flush();
  }

  static class PrintingVisitor extends SexpVisitor<String> {

    private StringBuilder out;
    private int charactersPerLine = 80;
    private boolean quote = true;
    private Context context;

    PrintingVisitor(Context context) {
      this.out = new StringBuilder();
      this.context = context;
    }
    
    public PrintingVisitor() {
      this.out = new StringBuilder();
    }

    public PrintingVisitor setCharactersPerLine(int charactersPerLine) {
      this.charactersPerLine = charactersPerLine;
      return this;
    }
    
    public PrintingVisitor setQuote(boolean quote) {
      this.quote = quote;
      return this;
    }
    
    public String print(SEXP exp) {
      exp.accept(this);
      return getResult();
    }

    @Override
    public void visit(ListVector list) {

      list = context.materialize(list);

      int index = 1;
      for(int i=0; i!= list.length(); ++i) {
        SEXP value = list.get(i);
        String name = list.getName(i);

        if(StringVector.isNA(name)) {
          name = "<NA>";
        }

        if(name.isEmpty()) {
          out.append("[[").append(index).append("]]\n");
        } else {
          out.append("$").append(name).append("\n");
        }
        value.accept(this);
        out.append("\n");
        index++;
      }
      printAttributes(list);
    }
    
    @Override
    public void visit(FunctionCall call) {
      out.append(Deparse.deparseExp(context, call));
      out.append("\n");
    }

    @Override
    protected void unhandled(SEXP exp) {
      out.append(exp.toString()).append('\n');
      printAttributes(exp);
    }

    @Override
    public String getResult() {
      return out.toString();
    }

    @Override
    public void visit(IntVector vector) {
      printVector(vector, Alignment.RIGHT, new IntPrinter(), "integer");
    }

    @Override
    public void visit(LogicalVector vector) {
      printVector(vector, Alignment.RIGHT, new LogicalPrinter(), "logical");
    }

    @Override
    public void visit(DoubleVector vector) {
      printVector(vector, Alignment.RIGHT, new RealPrinter(), "numeric");
    }

    @Override
    public void visit(StringVector vector) {
      printVector(vector, Alignment.LEFT, new StringPrinter().setQuotes(quote), "character");
    }

    @Override
    public void visit(ComplexVector vector) {
      printVector(vector, Alignment.RIGHT, new ComplexPrinter(), "complex");
    }

    @Override
    public void visit(RawVector vector) {
      printVector(vector, Alignment.RIGHT, new RawPrinter(), "raw");
    }   

    
    @Override
    public void visit(Null nullExpression) {
      out.append("NULL\n");
    }

    @Override
    public void visitSpecial(SpecialFunction special) {
      out.append(".Builtin(").append(StringLiterals.format(special.getName(), "NA"));
    }

    @Override
    public <T> void visit(ExternalPtr sexp) {
      Object instance = sexp.getInstance();
      if(instance == null) {
        out.append("<pointer: null>");
      } else {
        out.append(String.format("<pointer: %s@%x", 
            instance.getClass().getName(),
            java.lang.System.identityHashCode(instance)));
      }
    }

    private <T> void printVector(Iterable<T> iterable, Alignment align, Function<T, String> printer, String typeName) {
      Vector vector = (Vector)iterable;

      if(vector.isDeferred() && !vector.isConstantAccessTime()) {
        vector = context.materialize(vector);
      }

      if(vector.length() == 0) {
        out.append(typeName).append("(0)\n");
      } else {
        List<String> elements = Lists.newArrayList(Iterables.transform((Iterable<T>) vector, printer));
        
        SEXP dim = vector.getAttribute(Symbols.DIM);
        if(dim.length() == 2) {
          new MatrixPrinter(elements, align, vector.getAttributes());
        } else {
          new VectorPrinter(elements, align, vector.getAttributes());
        }
        printAttributes(vector);
      }
    }
    
    private void printAttributes(SEXP sexp) {
      for(PairList.Node node : sexp.getAttributes().nodes()) {
        if (!node.getTag().equals(Symbols.NAMES) &&
            !node.getTag().equals(Symbols.DIM) &&
            !node.getTag().equals(Symbols.DIMNAMES) &&
             node.getValue() != Null.INSTANCE) {
          out.append("attr(," + new StringPrinter().apply(node.getName()) + ")\n");
          node.getValue().accept(this);
        }
      }
    }


    private enum Alignment {
      LEFT, RIGHT
    }

    private class VectorPrinter {
      private List<String> elements;
      private final Alignment elementAlign;
      private int maxElementWidth;
      private int maxIndexWidth;
      private int elementsPerLine;
      private AtomicVector names;
      private ListVector dimnames;

      private VectorPrinter(List<String> elements, Alignment elementAlign, AttributeMap attributes) {
        this.elements = elements;
        this.elementAlign = elementAlign;
        this.names = (AtomicVector)attributes.getNamesOrNull();
        this.dimnames = attributes.getDimNamesOrEmpty();
        if(hasNames()) {
          elementAlign = Alignment.RIGHT;
        }
        calcMaxElementWidth();
        calcMaxIndexWidth();
        calcElementsPerLine();
        if(dimnames.length() != 0) {
          printDimnames();
        }
        print();
      }
      
      private boolean hasNames() {
        return names != Null.INSTANCE;
      }

      private void calcMaxElementWidth() {
        for(String s : elements) {
          if(s.length() > maxElementWidth) {
            maxElementWidth = s.length();
          }
        }
        if(hasNames()) {
          for(int i=0;i!=elements.size();++i) {
            int nameLength = name(i).length();
            if(nameLength > maxElementWidth) {
              maxElementWidth = nameLength;
            }
          }
        }
      }

      private void calcMaxIndexWidth() {
        maxIndexWidth = (int)Math.ceil(Math.log10(elements.size()));
      }
      
      private int rowHeaderWidth() {
        if(hasNames()) {
          return 0;
        } else {
          return maxIndexWidth + 2;
        }
      }

      private void calcElementsPerLine() {
        elementsPerLine = (charactersPerLine - rowHeaderWidth()) / (maxElementWidth+1);
        if(elementsPerLine < 1) {
          elementsPerLine = 1;
        }
      }

      private void print() {
        int index = 0;
        while(index < elements.size()) {
          
          if(hasNames()) {
            printNames(index);
          } else {
            printIndex(index);
          }
          printRow(index);
          index += elementsPerLine;
        }
      }

      private void printIndex(int index) {
        appendAligned(String.format("[%d] ", index+1), maxIndexWidth+2, Alignment.RIGHT);
      }
      
      private void printNames(int startIndex) {
        for(int i=0;i!=elementsPerLine && (startIndex+i)<elements.size();++i) {
          if(i > 0) {
            out.append(' ');
          }
          appendAligned(name(startIndex+i), maxElementWidth, elementAlign);
        }
        out.append("\n");
      }
      
      private String name(int index) {
        StringVector nameVector = (StringVector)names;
        if(nameVector.isElementNA(index)) {
          return "<NA>";
        } else {
          return nameVector.getElementAsString(index);
        }
      }

      private void printRow(int startIndex) {
        for(int i=0;i!=elementsPerLine && (startIndex+i)<elements.size();++i) {
          if(i > 0) {
            out.append(' ');
          }
          appendAligned(elements.get(startIndex+i), maxElementWidth, elementAlign);
        }
        out.append('\n');
      }
  
      private void printDimnames() {
        SEXP dims = dimnames.get(0);
        for(int i = 0; i != elementsPerLine && i < dims.length(); ++i) {
          out.append(' ');
          appendAligned(dims.getElementAsSEXP(i).asString(), maxElementWidth, elementAlign);
        }
        out.append('\n');
      }

      private void appendAligned(String s, int size, Alignment alignment) {
        if(alignment == Alignment.LEFT) {
          out.append(s);
        }
        for(int i=s.length(); i<size; ++i) {
          out.append(' ');
        }
        if(alignment == Alignment.RIGHT) {
          out.append(s);

        }
      }
    }


    private class MatrixPrinter {
      private List<String> elements;
      private final Alignment elementAlign;
      private int colWidth;
      private int maxRowHeaderWidth;
      
      private int rows;
      private int cols;
      private Vector rowNames = Null.INSTANCE;
      private Vector colNames = Null.INSTANCE;
      
      private MatrixPrinter(List<String> elements, Alignment elementAlign, AttributeMap attributes) {
        this.elements = elements;
        this.elementAlign = elementAlign;
        Vector dim = (Vector)attributes.get(Symbols.DIM);
        rows = dim.getElementAsInt(0);
        cols = dim.getElementAsInt(1);
        
        SEXP dimnames = (Vector)attributes.get(Symbols.DIMNAMES);
        if(dimnames.length() == 2) {
          rowNames = unpackRowNames((Vector)dimnames.getElementAsSEXP(0));
          colNames = dimnames.getElementAsSEXP(1);
        }
        
        calcMaxRowHeaderWidth();
        calcColumnWidth();
        print();
      }

      private Vector unpackRowNames(Vector rowNames) {
        if(RowNamesVector.isOldCompactForm(rowNames)) {
          return RowNamesVector.fromOldCompactForm(rowNames);
        } else { 
          return rowNames;
        }
      }

      private String colHeader(int col) {
        if(colNames == Null.INSTANCE) {
          return "[," + (col+1) + "]";
        } else {
          return naToString(colNames.getElementAsString(col));
        }
      }
      
      private String rowHeader(int row) {
        if(rowNames == Null.INSTANCE) {
          return "[" + (row+1) + ",]";
        } else {
          return naToString(rowNames.getElementAsString(row));
        }
      }
      
      private String naToString(String x) {
        if(x == null) {
          return "NA";
        } else {
          return x;
        }
      }
      
      private void calcMaxRowHeaderWidth() {
        for(int i=0;i!=rows;++i) {
          int headerLength = rowHeader(i).length();
          if(headerLength > maxRowHeaderWidth) {
            maxRowHeaderWidth = headerLength;
          }
        }
      }
      
      private void calcColumnWidth() {
        for(int i=0;i!=cols;++i) {
          int headerLength = colHeader(i).length();
          if(headerLength > colWidth) {
            colWidth = headerLength;
          }
        }
        for(String element : elements) {
          if(element.length() > colWidth) {
            colWidth = element.length();
          }
        }
      }
      
      private void print() {
        printColumnHeaders();
        
        for(int i=0; i!=rows;++i) {
          appendAligned(rowHeader(i), maxRowHeaderWidth, Alignment.RIGHT);
          for(int j=0;j!=cols;++j) {
            out.append(' ');
            appendAligned(elements.get(Indexes.matrixIndexToVectorIndex(i, j, rows, cols)), colWidth, elementAlign);
          }
          out.append('\n');
        }
      }

      private void printColumnHeaders() {
        for(int i=0;i!=maxRowHeaderWidth;++i) {
          out.append(' ');
        }
        for(int j=0;j!=cols;++j) {
          out.append(' ');
          appendAligned(colHeader(j), colWidth, elementAlign);
        }
        out.append('\n');
      }
      
      private void appendAligned(String s, int size, Alignment alignment) {
        if(alignment == Alignment.LEFT) {
          out.append(s);
        }
        for(int i=s.length(); i<size; ++i) {
          out.append(' ');
        }
        if(alignment == Alignment.RIGHT) {
          out.append(s);
        }
      }
    }
  
  }
}
