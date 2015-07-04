/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Materialize;
import org.renjin.parser.StringLiterals;
import org.renjin.primitives.print.*;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.io.IOException;
import java.util.List;

public class Print {

  private Print() {}

  @Internal("print.default")
  @Materialize
  public static SEXP printDefault(@Current Context context, SEXP expression, SEXP digits, boolean quote, SEXP naPrint,
      SEXP printGap, SEXP right, SEXP max, SEXP useSource, SEXP noOp) throws IOException {

    PrintingVisitor visitor = new PrintingVisitor(context)
    .setCharactersPerLine(80)
    .setQuote(quote);
    expression.accept(visitor);

    context.getSession().getStdOut().print(visitor.getResult());
    context.getSession().getStdOut().flush();
    context.setInvisibleFlag();
    return expression;

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

  @Internal("print.function")
  public static void printFunction(@Current Context context, SEXP x, boolean useSource) throws IOException {
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
      int index = 1;
      for(int i=0; i!= list.length(); ++i) {
        SEXP value = list.get(i);
        String name = list.getName(i);

        if(StringVector.isNA(name)) {
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
      printVector(vector, Alignment.LEFT, new StringPrinter().withQuotes(quote), "character");
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

    private <T> void printVector(Iterable<T> vector, Alignment align, Function<T, String> printer, String typeName) {
      SEXP sexp = (SEXP)vector;
      
      if(sexp.length() == 0) {
        out.append(typeName).append("(0)\n");
      } else {
        List<String> elements = Lists.newArrayList(Iterables.transform(vector, printer));
        
        SEXP dim = sexp.getAttribute(Symbols.DIM);
        if(dim.length() == 2) {
          new MatrixPrinter(elements, align, sexp.getAttributes());
        } else {
          new VectorPrinter(elements, align, sexp.getAttributes());
        }
        printAttributes(sexp);
      }
    }

    private void printAttributes(SEXP sexp) {
      for(PairList.Node node : sexp.getAttributes().nodes()) {
        if(!node.getTag().equals(Symbols.NAMES) &&
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

      private VectorPrinter(List<String> elements, Alignment elementAlign, AttributeMap attributes) {
        this.elements = elements;
        this.elementAlign = elementAlign;
        this.names = (AtomicVector)attributes.getNamesOrNull();
        if(hasNames()) {
          elementAlign = Alignment.RIGHT;
        }
        calcMaxElementWidth();
        calcMaxIndexWidth();
        calcElementsPerLine();
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
          return colNames.getElementAsString(col);
        }
      }
      
      private String rowHeader(int row) {
        if(rowNames == Null.INSTANCE) {
          return "[" + (row+1) + ",]";
        } else {
          return rowNames.getElementAsString(row);
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
