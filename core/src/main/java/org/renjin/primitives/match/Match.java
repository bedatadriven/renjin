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
package org.renjin.primitives.match;

import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.Contexts;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.sexp.*;

import java.util.HashMap;


/**
 * Default implementations of match() related functions.
 */
public class Match {

  private static final int UNMATCHED = -1;
  private static final int MULTIPLE_MATCH = -2;

  private Match() { }

  /**
   * match returns a vector of the positions of (first) matches of its first argument in its second.
   * @param search vector or NULL: the values to be matched.
   * @param table vector or NULL: the values to be matched against.
   * @param noMatch the value to be returned in the case when no match is found. Note that it is coerced to integer.
   * @param incomparables a vector of values that cannot be matched. Any value in x matching a value in this vector is assigned the nomatch value.
   *        For historical reasons, FALSE is equivalent to NULL.
   * @return
   */
  @Internal
  public static IntVector match(Vector search, Vector table, int noMatch, AtomicVector incomparables) {
    //For historical reasons, FALSE is equivalent to NULL.
    if(incomparables.equals( LogicalVector.FALSE ) ) {
      incomparables = Null.INSTANCE;
    }

    // We need to handle factors specially here -
    // treat them as strings if one of the other arguments
    // is a string
    if(search instanceof StringVector || table instanceof StringVector) {
      if(search.inherits("factor")) {
        search = new FactorString(search);
      }
      if(table.inherits("factor")) {
        table = new FactorString(table);
      }
    }
    
    
    // Check for special cases that we can handle quickly...
    
    // Are we matching against an unadulterated row.names vector?
    IntSequence sequence = isStringSequence(table);
    if(sequence != null && sequence.getBy() == 1 && incomparables.length() == 0) {
      return matchAgainstStringSequence(search, sequence, noMatch);
    }

    Vector.Type commonType = Vector.Type.widest(search.getVectorType(), table.getVectorType());
    search = commonType.to(search);
    table = commonType.to(table);

    int[] matches = new int[search.length()];
    for(int i=0;i!=search.length();++i) {
      if( incomparables.contains(search, i)) {
        matches[i] = noMatch;
      } else {
        int pos;
        if(search.isElementNA(i)) {
          pos = indexOfNA(table);
        } else {
          pos = table.indexOf(search, i, 0);
        }
        matches[i] = pos >= 0 ? pos+1 : noMatch;
      }
    }
    return IntArrayVector.unsafe(matches);
  }

  /**
   * Match a vector against a string sequence, for example as.character(1:1000)
   */
  private static IntVector matchAgainstStringSequence(Vector search, IntSequence sequence, int noMatch) {
    assert sequence.getBy() == 1;
    
    int from = sequence.getFrom();
    int length = sequence.length();
    
    int matches[] = new int[search.length()];
    
    // Keep track of whether search and sequence are identical
    // If they are, we can return a new sequence rather than an array
    boolean identity = (search.length() == sequence.length());
    
    for(int i=0;i!=search.length();++i) {
      String toMatch = search.getElementAsString(i);
      if(toMatch == null) {
        matches[i] = noMatch;
        identity = false;
      } else {
        try {
          int index = Integer.parseInt(toMatch) - from + 1;
          if(index > length) {
            matches[i] = noMatch;
            identity = false;
          } else {
            matches[i] = index;
            if(index != i + 1) {
              identity = false;
            }
          }
        } catch (NumberFormatException ignored) {
          matches[i] = noMatch;
          identity = false;
        }
      }
    }
    if(identity) {
      return new IntSequence(1, 1, search.length());
    }
    
    return IntArrayVector.unsafe(matches);
  }

  private static IntSequence isStringSequence(Vector table) {
    if(table instanceof ConvertingStringVector) {
      ConvertingStringVector wrapper = (ConvertingStringVector) table;
      if (wrapper.getOperand() instanceof IntSequence) {
        return (IntSequence) wrapper.getOperand();
      }
    }
    return null;
  }

  private static IntVector matchUsingStringHash(Vector search, Vector table, int noMatch, AtomicVector incomparables) {

    // Build HashMap... 
    // If items are repeated in the table, only the first is used.
    HashMap<String, Integer> lookup = new HashMap<>();
    for (int i = 0; i < table.length(); i++) {
      String tableElement = table.getElementAsString(i);
      if(!lookup.containsKey(tableElement)) {
        lookup.put(tableElement, i);
      }
    }

    for (int i = 0; i < incomparables.length(); i++) {
      lookup.put(incomparables.getElementAsString(i), noMatch);
    }
    
    int match[] = new int[search.length()];
    for (int i = 0; i < search.length(); i++) {
      String toMatch = search.getElementAsString(i);
      Integer index = lookup.get(toMatch);
      if(index == null) {
        match[i] = noMatch;
      } else {
        match[i] = index;
      }
    }
    
    return new IntArrayVector(match);
  }

  private static int indexOfNA(Vector table) {
    for(int i=0;i!=table.length();++i) {
      if(table.isElementNA(i)) {
        return i;
      }
    }
    return -1;
  }
  

  /**
   * pmatch seeks matches for the elements of its first argument among those of its second.
   *
   * The behaviour differs by the value of duplicates.ok. Consider first the case
   * if this is true. First exact matches are considered, and the positions of the
   * first exact matches are recorded. Then unique partial matches are considered,
   * and if found recorded. (A partial match occurs if the whole of the element of x
   * matches the beginning of the element of table.) Finally, all remaining elements of
   * x are regarded as unmatched. In addition, an empty string can match nothing, not even an
   * exact match to an empty string. This is the appropriate behaviour for partial matching
   * of character indices, for example.
   *
   * <p>If duplicates.ok is FALSE, values of table once matched are excluded from the
   * search for subsequent matches. This behaviour is equivalent to the R algorithm
   * for argument matching, except for the consideration of empty strings (which in
   * argument matching are matched after exact and partial matching to any remaining arguments).
   *
   * @param x the values to be matched
   * @param table the values to be matched against: converted to a character vector.
   * @param noMatch the value to be returned at non-matching or multiply partially matching positions.
   * @param duplicatesOk can elements be in table be matched to multiple elements in x?
   * @return An integer vector (possibly including NA if nomatch = NA) of the same length as x,
   * giving the indices of the elements in table which matched, or {@code nomatch}.
   */
  @Internal
  public static IntVector pmatch(StringVector x, StringVector table, int noMatch, boolean duplicatesOk) {
    return commonStringMatch(x, table, noMatch, noMatch, duplicatesOk);
  }
  
  @Internal
  public static IntVector charmatch(StringVector x, StringVector table, int noMatch) {
    // I don't really understand the difference between charmatch and pmatch:
    // it seems that for pmatch, if the search string partially matches more than one
    // element in table, then it's returns the no match value (default = NA)
    // 
    // charmatch() on the ohter hand seems to return 0 for the special case.
    return commonStringMatch(x, table, noMatch, 0, true );
  }
  

  /**
   * Common implementation for pmatch and charmatch
   * @param x
   * @param table
   * @param unmatchedCode the value to return when there is no match
   * @param duplicatePartialsCode the value to return when a value in x partially matches multiple values in table
   * @return
   */
  private static IntVector commonStringMatch(StringVector x, StringVector table,
      int unmatchedCode, int duplicatePartialsCode, boolean duplicatesOk) {
    IntArrayVector.Builder result = new IntArrayVector.Builder(x.length());
    boolean matchedTable[] = new boolean[table.length()];
    boolean matchedSearch[] = new boolean[x.length()];

    // first pass : exact matches
    for(int i=0;i!= x.length();++i) {
      String toMatch = pmatchElementAt(x, i);
      int match = exactMatch(toMatch, table);
      if(match != UNMATCHED && (duplicatesOk || !matchedTable[match])) {
        result.set(i, match+1);
        matchedTable[match] = true;
        matchedSearch[i] = true;
      }
    }

    // second pass : partial matches
    for(int i=0;i!= x.length();++i) {
      if(!matchedSearch[i]) {
        String toMatch = pmatchElementAt(x, i);
        int match = uniquePartialMatch(toMatch, table);
        if(match == UNMATCHED) {
          result.set(i, unmatchedCode);
        } else if(match == MULTIPLE_MATCH) {
          result.set(i, duplicatePartialsCode);
        } else if(duplicatesOk || !matchedTable[match]) {
          result.set(i, match+1);
          matchedTable[match] = true;
        }
      }
    }
    return result.build();
  }

  private static int exactMatch(String toMatch, StringVector table) {
    for(int i=0;i!=table.length();++i) {
      String t = pmatchElementAt(table, i);
      if(toMatch.equals(t)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Attempts to match a string value within in a table of values
   * @param toMatch
   * @param table
   * @return the index of the unique martial match, UNMATCHED if there were no partial matches,
   * and MULTIPLE_MATCH if there were multiple partial matches
   */
  private static int uniquePartialMatch(String toMatch, StringVector table) {
    int partialMatch = UNMATCHED;
    for(int i=0;i!=table.length();++i) {
      String t = pmatchElementAt(table, i);
      if(t.startsWith(toMatch)) {
        // if we've previously found a partial match, abort
        if(partialMatch != UNMATCHED) {
          return MULTIPLE_MATCH;
        }
        partialMatch = i;
      }
    }
    return partialMatch;
  }

  // NA values are treated as if they were the string constant "NA".
  private static String pmatchElementAt(StringVector vector, int i) {
    return vector.isElementNA(i) ? "NA" : vector.getElementAsString(i);
  }  
  
  @Internal("match.call")
  public static SEXP matchCall (@Current Context context, @Current Environment rho, SEXP definition, FunctionCall call, boolean expandDots,
                                SEXP environment) {
    
    Closure closure = null;
    if(definition instanceof Closure) {
      closure = (Closure)definition;
    } else if(definition == Null.INSTANCE) {

      /* Get the env that the function containing */
      /* matchcall was called from. */
      Context parentContext = Contexts.findCallingContext(context);
      if(parentContext.getFunction() instanceof Closure) {
        closure = (Closure) parentContext.getFunction();
      }
      if(closure == null) {
        throw new EvalException("match.call() was called from outside a function");
      }
    } else {
      throw new EvalException("match.call cannot use definition of type '%s'", definition.getTypeName());
    }
    
    PairList matched = ClosureDispatcher.matchArguments(closure.getFormals(), call.getArguments(), true);
    
    PairList.Builder expandedArgs = new PairList.Builder();
    for(PairList.Node node : matched.nodes()) {
      if(node.getValue() != Symbol.MISSING_ARG) {
        if(expandDots && node.getTag() == Symbols.ELLIPSES) {
          for(PairList.Node elipseNode : ((PairList)node.getValue()).nodes()) {
            expandedArgs.add(elipseNode.getRawTag(), elipseNode.getValue());
          }
        } else {
          expandedArgs.add(node.getTag(), node.getValue());
        }
      }
    }
    
    return new FunctionCall(call.getFunction(), expandedArgs.build());
  }
  
  /**
   * Returns an IntVector indices of elements that are {@code TRUE}.
   * 
   * <p>Note that the which() function in the base package handles 
   * array indices and names, this internal function simply returns
   * the indices
   */
  @Internal
  public static IntVector which(Vector x) {
    IntArrayVector.Builder indices = new IntArrayVector.Builder();
   
    Vector xn = x.getNames();
    StringArrayVector.Builder names = null;
    if(xn != Null.INSTANCE) {
      names = new StringArrayVector.Builder();
    }

    for(int i=0;i!=x.length();++i) {
      if(x.isElementTrue(i)) {
        indices.add(i+1);
        if(names != null) {
          names.add(xn.getElementAsString(i));
        }
      }
    }
    
    if(names != null) {
      indices.setAttribute(Symbols.NAMES, names.build());
    }
    
    return indices.build();
  }

  private static class FactorString extends StringVector {

    private final Vector factor;
    private final Vector levels;

    private FactorString(Vector factor) {
      super(AttributeMap.EMPTY);
      this.factor = factor;
      this.levels = (Vector) factor.getAttribute(Symbols.LEVELS);
    }

    @Override
    public int length() {
      return factor.length();
    }

    @Override
    protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getElementAsString(int index) {
      if(factor.isElementNA(index)) {
        return StringVector.NA;
      } else {
        int level = factor.getElementAsInt(index);
        return levels.getElementAsString(level-1);
      }
    }

    @Override
    public boolean isConstantAccessTime() {
      return true;
    }
  }
}

