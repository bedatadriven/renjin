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

package org.renjin.sexp;

/**
 * A visitor for iterating through an expression tree
 */
public class SexpVisitor<R> {


  protected void unhandled(SEXP exp) {
    System.out.println(exp.getClass().getName());
  }

  public void visit(CHARSEXP charExp) {
    unhandled(charExp);
  }

  public void visit(BuiltinFunction builtin) {
    unhandled(builtin);
  }

  public void visit(ComplexVector complexExp) {
    unhandled(complexExp);
  }

  public void visit(Environment environment) {
    unhandled(environment);
  }

  public void visit(ExpressionVector vector) {
    unhandled(vector);
  }

  public void visit(IntVector vector) {
    unhandled(vector);
  }

  public void visit(FunctionCall call) {
    unhandled(call);
  }

  public void visit(PairList.Node pairList) {
    unhandled(pairList);
  }

  public void visit(LogicalVector vector) {
    unhandled(vector);
  }

  public void visit(Null nullExpression) {
    unhandled(nullExpression);
  }

  public void visit(PrimitiveFunction primitive) {
    unhandled(primitive);
  }

  public void visit(Promise promise) {
    unhandled(promise);
  }

  public void visit(DoubleVector vector) {
    unhandled(vector);
  }

  public void visit(PromisePairList dotExp) {
    unhandled(dotExp);
  }

  public void visit(StringVector vector) {
    unhandled(vector);
  }

  public void visit(Symbol symbol) {
    unhandled(symbol);
  }

  public void visit(Closure closure) {
    unhandled(closure);
  }
  
  public void visit(RawVector vector) {
    unhandled(vector);
  }

  public R getResult() {
    throw new UnsupportedOperationException();
  }

  public void visitSpecial(SpecialFunction special) {
    unhandled(special);
  }

  public void visit(ListVector list) {
    unhandled(list);
  }


  public void visit(S4Object s4Object) {
    unhandled(s4Object);
  }

  
  public final void acceptAll(Iterable<SEXP> elements) {
    for(SEXP element : elements) {
      element.accept(this);
    }
  }

}
