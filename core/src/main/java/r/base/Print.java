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

package r.base;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.parser.ParseUtil;

import java.io.PrintStream;
import java.util.List;

public class Print {

  private Print() {}


  public static void print(PrintStream printStream, SEXP value, int charactersPerLine) {
    printStream.println(new PrintingVisitor(value,charactersPerLine).getResult());
  }

  public static String print(SEXP expression, int charactersPerLine) {
    return new PrintingVisitor(expression,charactersPerLine).getResult();
  }

  @Primitive("print.default")
  public static SEXP printDefault(@Current Context context, SEXP expression, SEXP digits, SEXP quote, SEXP naPrint,
                                    SEXP printGap, SEXP right, SEXP max, SEXP useSource, SEXP noOp) {

    String printed = new PrintingVisitor(expression,80).getResult();
    context.getGlobals().stdout.print(printed);
    context.getGlobals().stdout.flush();
    context.setInvisibleFlag();
    return expression;
  }

  @Primitive("print.function")
  public static void printFunction(@Current Context context, SEXP x, boolean useSource) {
    context.getGlobals().stdout.println(x.toString());
    context.getGlobals().stdout.flush();
  }

  static class PrintingVisitor extends SexpVisitor<String> {

    private StringBuilder out;
    private int charactersPerLine;

    PrintingVisitor(SEXP exp, int charactersPerLine) {
      this.out = new StringBuilder();
      this.charactersPerLine = charactersPerLine;

      exp.accept(this);
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
      printVector(vector, Alignment.RIGHT, new ParseUtil.IntPrinter());
    }

    @Override
    public void visit(LogicalVector vector) {
      printVector(vector, Alignment.RIGHT, new ParseUtil.LogicalPrinter());
    }

    @Override
    public void visit(DoubleVector vector) {
      printVector(vector, Alignment.RIGHT, new ParseUtil.RealPrinter());
    }

    @Override
    public void visit(StringVector vector) {
      printVector(vector, Alignment.LEFT, new ParseUtil.StringPrinter());
    }

    @Override
    public void visit(RawVector vector) {
      printVector(vector, Alignment.RIGHT, new ParseUtil.RawPrinter());
    }   

    @Override
    public void visit(Null nullExpression) {
      out.append("NULL\n");
    }

    @Override
    public void visitSpecial(SpecialFunction special) {
      out.append(".Primitive(").append(ParseUtil.formatStringLiteral(special.getName(), "NA"));
    }

    private <T> void printVector(Iterable<T> vector, Alignment align, Function<T, String> printer) {
      SEXP sexp = (SEXP)vector;
      List<String> elements = Lists.newArrayList(Iterables.transform(vector, printer));
      
      SEXP dim = sexp.getAttribute(Symbols.DIM);
      if(dim.length() == 2) {
        new MatrixPrinter(elements, align, sexp.getAttributes());
      } else {
        new VectorPrinter(elements, align, sexp.getAttributes());
      }
      printAttributes(sexp);
    }

    private void printAttributes(SEXP sexp) {
      for(PairList.Node node : sexp.getAttributes().nodes()) {
        if(!node.getTag().equals(Symbols.NAMES) &&
           !node.getTag().equals(Symbols.DIM) &&
           !node.getTag().equals(Symbols.DIMNAMES) &&
            node.getValue() != Null.INSTANCE) {
          out.append("attr(," + new ParseUtil.StringPrinter().apply(node.getName()) + ")\n");
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

      private VectorPrinter(List<String> elements, Alignment elementAlign, PairList attributes) {
        this.elements = elements;
        this.elementAlign = elementAlign;
        this.names = (AtomicVector)attributes.findByTag(Symbols.NAMES);
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
          return nameVector.getElement(index);
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
      
      private MatrixPrinter(List<String> elements, Alignment elementAlign, PairList attributes) {
        this.elements = elements;
        this.elementAlign = elementAlign;
        Vector dim = (Vector)attributes.findByTag(Symbols.DIM);
        rows = dim.getElementAsInt(0);
        cols = dim.getElementAsInt(1);
        
        SEXP dimnames = (Vector)attributes.findByTag(Symbols.DIMNAMES);
        if(dimnames.length() == 2) {
          rowNames = dimnames.getElementAsSEXP(0);
          colNames = dimnames.getElementAsSEXP(1);
        }
        
        calcMaxRowHeaderWidth();
        calcColumnWidth();
        print();
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
        int index = 0;
        
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
