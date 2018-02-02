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
package org.renjin.eval;

import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.ir.tac.IRArgument;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Matching between supplied arguments to a closure call and the
 * formally declared arguments of the closure.
 */
public class MatchedArgumentPositions {

  private final boolean[] matchedActuals;
  private String[] formalNames;
  private final int formalEllipses;
  private final int[] formalMatches;

  private int extraArgumentCount;

  MatchedArgumentPositions(String[] formalNames, int[] formalMatches, boolean[] matchedActuals, int formalEllipses) {
    this.formalNames = formalNames;
    this.formalMatches = formalMatches;

    this.matchedActuals = matchedActuals;
    this.formalEllipses = formalEllipses;
    extraArgumentCount = 0;
    for (int i = 0; i < matchedActuals.length; i++) {
      if(!matchedActuals[i]) {
        extraArgumentCount++;
      }
    }
  }

  public static MatchedArgumentPositions matchIRArguments(Closure closure, List<IRArgument> arguments) {
    String[] names  = new String[arguments.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = arguments.get(i).getName();
    }
    return new ArgumentMatcher(closure).match(names);
  }

  public static MatchedArgumentPositions matchArgumentBounds(Closure closure, List<ArgumentBounds> arguments) {
    String[] names  = new String[arguments.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = arguments.get(i).getName();
    }
    return new ArgumentMatcher(closure).match(names);
  }

  public Set<Symbol> getSuppliedFormals() {
    return getMatchedFormals().keySet();
  }

  /**
   * @return the index of the provided argument that matches to the given {@code formalIndex}, or -1 if there
   * is no match.
   */
  public int getActualIndex(int formalIndex) {
    return formalMatches[formalIndex];
  }

  public Map<Symbol, Integer> getMatchedFormals() {
    HashMap<Symbol, Integer> map = new HashMap<>();
    for (int i = 0; i < formalMatches.length; i++) {
      if(formalMatches[i] != -1) {
        map.put(Symbol.get(formalNames[i]), formalMatches[i]);
      }
    }
    return map;
  }

  public boolean hasExtraArguments() {
    return extraArgumentCount > 0;
  }

  public int getExtraArgumentCount() {
    return extraArgumentCount;
  }


  public boolean isExtraArgument(int actualIndex) {
    return !this.matchedActuals[actualIndex];
  }

  /**
   * @return the name of the {@code i}th formal argument
   */
  public Symbol getFormalName(int i) {
    return Symbol.get(formalNames[i]);
  }

  public int getFormalCount() {
    return formalNames.length;
  }

  /**
   *
   * @return true if all formal arguments are matched to a provided argument.
   */
  public boolean allFormalsMatched() {
    for (int i = 0; i < formalMatches.length; i++) {
      if(i != formalEllipses && formalMatches[i] == -1) {
        return false;
      }
    }
    return true;
  }
}
