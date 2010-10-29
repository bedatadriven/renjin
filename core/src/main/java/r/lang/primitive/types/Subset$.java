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

package r.lang.primitive.types;

import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.BinaryFunction;

/**
 *  The $ indexing operator
 *
 */
public class Subset$ extends BinaryFunction {

  /** The $ subset operator.
   * We need to be sure to only evaluate the first argument.
   * The second will be a symbol that needs to be matched, not evaluated.
   */
  @Override
  public EvalResult apply(LangExp call, EnvExp rho, SEXP list, SEXP index) {

    if(list instanceof PairList) {
      return new EvalResult( matchPartial((PairList)list, toString(index)) );

    } else if(list instanceof EnvExp) {
      return new EvalResult( ((EnvExp) list).findVariable(
          rho.getGlobalContext().symbol( toString(index) ) ) );

    } else if(list instanceof AtomicExp) {
      throw new EvalException("$ operator is invalid for atomic vectors");

    } else {
      return new EvalResult(  NilExp.INSTANCE );
    }
  }

  private SEXP matchPartial(PairList list, String indexName)  {
    SEXP partialMatch = null;
    int partialMatchCount = 0;

    for(ListExp node : ListExp.listNodes(list)) {
      String tag = tagName(node.getTag());
      if(tag != null) {
        if(indexName.equals(tag)) {
          return node.getValue();
        } else if(indexName.startsWith(tag)) {
          partialMatch = node.getValue();
          partialMatchCount++;
        }
      }
    }
    if(partialMatchCount == 1) {
      return partialMatch;
    }
    return NilExp.INSTANCE;
  }

  private String tagName(SEXP tag) {
    if(tag instanceof SymbolExp) {
      return ((SymbolExp) tag).getPrintName();
    } else {
      return null;
    }
  }

  private String toString(SEXP index) {
    if(index instanceof SymbolExp) {
      return ((SymbolExp) index).getPrintName();
    } else if(index instanceof StringExp) {
      return ((StringExp) index).get(0);
    } else {
      throw new EvalException("invalid subscript type '%s'", index.getTypeName());
    }
  }


}
