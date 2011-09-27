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

package r.compiler.ir.tree;

import com.google.common.collect.Maps;
import r.compiler.ReservedWords;
import r.compiler.ir.temp.Label;
import r.compiler.ir.tree.builders.DefaultCallBuilder;
import r.compiler.ir.tree.builders.IfStatement;
import r.lang.*;

import java.util.Map;

/**
 * Translates R's Abstract Syntax Tree (AST) -- the SEXP
 * hierarchy -- into an intermediate representation (IR) that is
 * more convenient to compile.
 */
public class IRFactory {

  private int labelCount = 0;
  private Map<Symbol, ReservedWords.IRBuilder> reserved;

  public IRFactory() {
    reserved = Maps.newHashMap();
    reserved.put(Symbol.get("if"), new IfStatement());
  }

  public StmList build(SEXP exp) {
    StmList list = new StmList();
    list.add( buildStm(exp) );

    return list;
  }

  public Exp buildExp(SEXP sexp) {
    if(sexp instanceof FunctionCall) {
      return getBuilder((FunctionCall) sexp).buildExp(this, (FunctionCall) sexp);

    } else if(sexp instanceof ListVector || sexp instanceof AtomicVector) {
        return new ConstantExp(sexp);

    } else if(sexp instanceof Symbol) {
        return new NameExp((Symbol) sexp);

    } else {
        throw new UnsupportedOperationException("Cannot transform: " + sexp.getClass());
    }
  }

  private ReservedWords.IRBuilder getBuilder(FunctionCall call) {
    if(call.getFunction() instanceof Symbol) {
      ReservedWords.IRBuilder builder = reserved.get(call.getFunction());
      if(builder != null) {
        return builder;
      }
    }
    return new DefaultCallBuilder();
  }

  public Statement buildStm(SEXP sexp) {
     if(sexp instanceof FunctionCall) {
      return getBuilder((FunctionCall) sexp).buildStm(this, (FunctionCall) sexp);

    } else if(sexp instanceof ListVector || sexp instanceof AtomicVector) {
        return new ExpStm( new ConstantExp(sexp) );

    } else if(sexp instanceof Symbol) {
        return new ExpStm( new NameExp((Symbol) sexp) );

    } else {
        throw new UnsupportedOperationException("Cannot transform: " + sexp.getClass());
    }
  }

  public Label newLabel() {
    return new Label(++labelCount);
  }
}
