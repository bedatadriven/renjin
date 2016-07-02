package org.renjin.eval;

import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

/**
 * Matches a named list of arguments to named formals
 */
public class ArgumentMatching {


  /**
   * Matches a list of supplied arguments by names to a closure's declared 
   * formal arguments.
   * 
   * @param formalNames an array of formal names
   * @param actualNames an array of supplied
   * @return an array with an element for each actual, with the index of the matched formal, or -1 if it belongs to
   */
  public int[] match(Symbol[] formalNames, String[] actualNames) {

    boolean actualMatched[] = new boolean[actualNames.length];
    boolean formalMatched[] = new boolean[formalNames.length];

    int match[] = new int[actualMatched.length];
    
    // Do exact matching first....
    for (int i = 0; i < formalNames.length; i++) {
      Symbol formal = formalNames[i];
      if(formal != Symbols.ELLIPSES) {

        int exactMatch = findExactMatch(formal, actualNames);
        if(exactMatch != -1) {
          formalMatched[i] = true;
          actualMatched[exactMatch] = true;
          match[exactMatch] = i;
        }
      }
    }
    
    // Partial matching by name
    for (int i = 0; i < actualNames.length; i++) {
      if(!actualMatched[i]) {
        String actualName = actualNames[i];
        if(actualName != null) {
          int partialMatch = findPartialMatch(actualNames[i], formalNames);
          if(partialMatch != -1) {
            match[i] = partialMatch;
            formalMatched[partialMatch] = true;
            actualMatched[i] = true;
          }
        }
      }
    }

    throw new UnsupportedOperationException("TODO");
//    // Match the remaining actuals without a name by position
//    int nextActual = 0;
//    while( (nextActual = nextUnmatched(actualMatched, nextActual)) != -1) {
//      int nextFormal = nextUnmatched(formalMatched, 0);
//      if(nextFormal == -1) {
//        throw new InvalidStateException(""
//      }
//      
//      
//    }
  }

  private int nextUnmatched(boolean[] matched, int start) {
    for(int i=start; i<matched.length;++i) {
      if(!matched[i]) {
        return i;
      }
    }
    return -1;
  }


  private int findExactMatch(Symbol formal, String[] actualNames) {
    String formalName = formal.getPrintName();
    int exactMatch = -1;

    for (int i = 0; i < actualNames.length; i++) {
      String actualName = actualNames[i];
      if(actualName != null) {
        if(actualName.equals(formalName)) {
          if(exactMatch == -1) {
            exactMatch = i;
          } else {
            throw new EvalException(String.format("Multiple named values provided for argument '%s'", formal.getPrintName()));
          }
        }
      }
    }
    return exactMatch;
  }

  private int findPartialMatch(String actualName, Symbol[] formalNames) {
    int partialMatch = -1;

    for (int i = 0; i < formalNames.length; i++) {
      Symbol formalName = formalNames[i];
      // only partially match on formal arguments preceding ELIPSES
      if (formalName == Symbols.ELLIPSES) {
        break;
      }
      if (formalName.getPrintName().startsWith(actualName)) {
        if (partialMatch == -1) {
          partialMatch = i;
        } else {
          throw new EvalException(String.format(
              "Provided argument '%s' matches multiple named formal arguments", actualName));        
        }
      }
    }
    return partialMatch;
  }
}
