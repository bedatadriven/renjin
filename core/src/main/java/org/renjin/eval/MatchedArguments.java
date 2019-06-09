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

import org.renjin.sexp.*;

/**
 * Matching between supplied arguments to a closure call and the
 * formally declared arguments of the closure.
 */
public class MatchedArguments {
  private final String[] actualNames;
  private final SEXP[] actualValues;
  private final MatchedArgumentPositions matchedPositions;

  public MatchedArguments(MatchedArgumentPositions matchedPositions, String[] actualNames, SEXP[] actualValues) {
    this.actualNames = actualNames;
    this.actualValues = actualValues;
    this.matchedPositions = matchedPositions;

    for (int i = 0; i < actualNames.length; i++) {
      if("".equals(actualNames[i])) {
        throw new IllegalStateException();
      }
    }
  }

  /**
   *
   * @return the total number of formal arguments
   */
  public int getFormalCount() {
    return matchedPositions.getFormalCount();
  }

  public String[] getActualNames() {
    return actualNames;
  }

  public int getActualCount() {
    return actualValues.length;
  }

  /**
   * @return {@code true} if the formal at index {@code formalIndex} is the ellipses (...)
   */
  public boolean isFormalEllipses(int formalIndex) {
    return matchedPositions.getFormalSymbol(formalIndex) == Symbols.ELLIPSES;
  }

  public Symbol getFormalSymbol(int formalIndex) {
    return matchedPositions.getFormalSymbol(formalIndex);
  }

  public PromisePairList buildExtraArgumentList() {
    PromisePairList.Builder promises = new PromisePairList.Builder();
    for (int actualIndex = 0; actualIndex < actualValues.length; actualIndex++) {
      if(matchedPositions.isExtraArgument(actualIndex)) {
        String name = actualNames[actualIndex];
        SEXP tag;
        if(name == null) {
          tag = Null.INSTANCE;
        } else {
          tag = Symbol.get(name);
        }
        promises.add(tag, actualValues[actualIndex] );
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
      throw new EvalException("Argument \"" + getFormalSymbol(formalIndex).getPrintName() + "\" is missing, with no default");
    }
    return actualValues[actualIndex];
  }

  /**
   * Finds the index of an actual that is (exactly) named {@code name}, or -1 if no match is found.
   */
  public int findActualIndexByName(String name) {
    for (int i = 0; i < actualNames.length; i++) {
      if(name.equals(actualNames[i])) {
        return i;
      }
    }
    return -1;
  }

  public SEXP[] getActualValues() {
    return actualValues;
  }

  public String getFormalName(int formalIndex) {
    return matchedPositions.getFormalName(formalIndex);
  }
}
