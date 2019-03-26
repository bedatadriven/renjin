/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.eval;

import org.renjin.sexp.PromisePairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

/**
 * Matching between supplied arguments to a closure call and the
 * formally declared arguments of the closure.
 */
public class MatchedArguments {
  private SEXP[] actualTags;
  private final SEXP[] actualValues;
  private final MatchedArgumentPositions matchedPositions;

  public MatchedArguments(MatchedArgumentPositions matchedPositions, SEXP[] actualTags, SEXP[] actualValues) {
    this.actualTags = actualTags;
    this.actualValues = actualValues;
    this.matchedPositions = matchedPositions;
  }

  /**
   *
   * @return the total number of formal arguments
   */
  public int getFormalCount() {
    return matchedPositions.getFormalCount();
  }

  /**
   * @return {@code true} if the formal at index {@code formalIndex} is the ellipses (...)
   */
  public boolean isFormalEllipses(int formalIndex) {
    return matchedPositions.getFormalName(formalIndex) == Symbols.ELLIPSES;
  }

  public Symbol getFormalName(int formalIndex) {
    return matchedPositions.getFormalName(formalIndex);
  }

  public PromisePairList buildExtraArgumentList() {
    PromisePairList.Builder promises = new PromisePairList.Builder();
    for (int actualIndex = 0; actualIndex < actualValues.length; actualIndex++) {
      if(matchedPositions.isExtraArgument(actualIndex)) {
        promises.add( actualTags[actualIndex],  actualValues[actualIndex] );
      }
    }
    return promises.build();
  }

  /**
   * @return the index of the provided argument that matches to the given {@code formalIndex}, or -1 if there
   * is no match.
   */
  public int getActualIndex(int formalIndex) {
    return matchedPositions.getActualIndex(formalIndex);
  }

  public SEXP getActualValue(int actualIndex) {
    return actualValues[actualIndex];
  }

  public boolean areAllFormalsMatched() {
    return matchedPositions.allFormalsMatched();
  }

  public SEXP getActualForFormal(int formalIndex, SEXP defaultValue) {
    int actualIndex = getActualIndex(formalIndex);
    if(actualIndex == -1) {
      return defaultValue;
    } else {
      return actualValues[actualIndex];
    }
  }

  public SEXP getActualForFormal(int formalIndex) {
    int actualIndex = getActualIndex(formalIndex);
    if(actualIndex == -1) {
      throw new EvalException("Argument \"" + getFormalName(formalIndex).getPrintName() + "\" is missing, with no default");
    }
    return actualValues[actualIndex];
  }
}
