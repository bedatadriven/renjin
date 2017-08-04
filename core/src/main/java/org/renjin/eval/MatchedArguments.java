/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.renjin.sexp.*;

import java.util.*;


/**
 * Matching between supplied arguments to a closure call and the
 * formally declared arguments of the closure.
 */
public class MatchedArguments {

  private final boolean[] matchedActuals;
  private String[] formalNames;
  private final int[] formalMatches;

  private int extraArgumentCount;

  MatchedArguments(String[] formalNames, int[] formalMatches, boolean[] matchedActuals) {
    this.formalNames = formalNames;
    this.formalMatches = formalMatches;

    this.matchedActuals = matchedActuals;
    extraArgumentCount = 0;
    for (int i = 0; i < matchedActuals.length; i++) {
      if(!matchedActuals[i]) {
        extraArgumentCount++;
      }
    }
  }

  public static MatchedArguments matchIRArguments(Closure closure, List<IRArgument> arguments) {
    String[] names  = new String[arguments.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = arguments.get(i).getName();
    }
    return new ArgumentMatcher(closure).match(names);
  }

  public static MatchedArguments matchArgumentBounds(Closure closure, List<ArgumentBounds> arguments) {
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

  public Symbol getFormal(int i) {
    return Symbol.get(formalNames[i]);
  }

  public int getFormalCount() {
    return formalNames.length;

  }
}
