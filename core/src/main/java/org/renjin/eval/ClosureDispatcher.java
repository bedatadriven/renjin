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

import org.renjin.sexp.PairList;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;


public class ClosureDispatcher {


  /**
   * Matches arguments to actuals
   * @param formals
   * @param actuals
   * @param populateMissing
   * @return
   */
  public static PairList matchArguments(PairList formals, PairList actuals, boolean populateMissing) {

    ArgumentMatcher matcher = new ArgumentMatcher(formals);
    MatchedArguments matching = matcher.match(actuals);

    PairList.Builder result = new PairList.Builder();
    for(int formalIndex = 0; formalIndex < matching.getFormalCount(); ++formalIndex) {

      if(matching.isFormalEllipses(formalIndex)) {
        result.add(Symbols.ELLIPSES, matching.buildExtraArgumentList());

      } else {
        int actualIndex = matching.getActualIndex(formalIndex);
        if(actualIndex == -1) {
          if(populateMissing) {
            result.add(matching.getFormalSymbol(formalIndex), Symbol.MISSING_ARG);
          }
        } else {
          result.add(matching.getFormalSymbol(formalIndex), matching.getActualValue(actualIndex));
        }
      }
    }

    return result.build();
  }

}
