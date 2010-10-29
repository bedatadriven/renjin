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

package r.interpreter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import r.lang.*;
import r.parser.ParseUtil;

import java.util.List;

class PrintingVisitor extends SexpVisitor<String> {

  private StringBuilder out;
  private int charactersPerLine;

  PrintingVisitor(SEXP exp, int charactersPerLine) {
    this.out = new StringBuilder();
    this.charactersPerLine = charactersPerLine;

    exp.accept(this);
  }

  @Override
  public void visit(ListExp listExp) {
    int index = 1;
    for(SEXP exp : listExp) {
      out.append("[[").append(index).append("]]\n");
      exp.accept(this);
      out.append("\n");
      index++;
    }
  }

  @Override
  public void visit(IntExp intExp) {
    printVector(intExp, Alignment.RIGHT, new ParseUtil.IntPrinter());
  }

  @Override
  public void visit(LogicalExp logicalExp) {
    printVector(logicalExp, Alignment.RIGHT, new ParseUtil.LogicalPrinter());
  }

  @Override
  public void visit(RealExp realExp) {
    printVector(realExp, Alignment.RIGHT, new ParseUtil.RealPrinter());
  }

  @Override
  public void visit(StringExp stringExp) {
    printVector(stringExp, Alignment.LEFT, new ParseUtil.StringPrinter());
  }

  @Override
  public void visit(NilExp nilExp) {
    out.append("NULL");
  }

  @Override
  public void visitSpecial(SpecialExp specialExp) {
    out.append(".Primitive(").append(ParseUtil.formatStringLiteral(specialExp.getName(), "NA"));
  }

  private <T> void printVector(Iterable<T> intExp, Alignment align, Function<T, String> printer) {
    List<String> elements = Lists.newArrayList(Iterables.transform(intExp, printer));
    new VectorPrinter(elements, align);
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

    private VectorPrinter(List<String> elements, Alignment elementAlign) {
      this.elements = elements;
      this.elementAlign = elementAlign;
      calcMaxElementWidth();
      calcMaxIndexWidth();
      calcElementsPerLine();
      print();
    }

    private void calcMaxElementWidth() {
      for(String s : elements) {
        if(s.length() > maxElementWidth) {
          maxElementWidth = s.length();
        }
      }
    }

    private void calcMaxIndexWidth() {
      maxIndexWidth = (int)Math.ceil(Math.log10(elements.size()));
    }

    private void calcElementsPerLine() {
      elementsPerLine = (charactersPerLine - (maxIndexWidth+2)) / (maxElementWidth+1);
    }

    private void print() {
      int index = 0;
      while(index < elements.size()) {
        printIndex(index);
        printRow(index);
        index += elementsPerLine;
      }
    }

    private void printIndex(int index) {
      appendAligned(String.format("[%d]", index+1), maxIndexWidth+2, Alignment.RIGHT);
    }

    private void printRow(int startIndex) {
      for(int i=0;i!=elementsPerLine && (startIndex+i)<elements.size();++i) {
        out.append(' ');
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

  @Override
  protected void unhandled(SEXP exp) {
    out.append(exp.toString()).append('\n');
  }

  @Override
  public String getResult() {
    return out.toString();
  }
}
