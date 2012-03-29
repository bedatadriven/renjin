package org.renjin.primitives.models;

import org.renjin.eval.EvalException;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;


/**
 * Model formulas in R are defined with a Dynamic Specific Language (DSL)
 * that describe a {@link Formula}. The DSL uses the same Abstract Syntax
 * Tree (AST) as R expressions, but certain functions are interpreted differently 
 * in this context.
 * 
 */
public class FormulaInterpreter {
  
  private Symbol response;
  private int intercept = 1;
  
  private static final Symbol tilde = Symbol.get("~");
  private static final Symbol UNION = Symbol.get("+");
  private static final Symbol EXPAND_TERMS = Symbol.get("*");
  private static final Symbol DIFFERENCE = Symbol.get("-");
  private static final Symbol GROUP = Symbol.get("(");
  
  public Formula interpret(FunctionCall call) {
    if(call.getFunction() != tilde) {
      throw new EvalException("expected model formula (~)");
    }
    
    TermList terms = new TermList();
    if(call.getArguments().length() == 1) {
      response = null;
      add(terms, call.getArgument(0));
    } else if(call.getArguments().length() == 2) {
      response = call.getArgument(0);
      add(terms, call.getArgument(1));
    }
    
    return new Formula(response, intercept, terms.sorted());
  }
  
  /**
   * Build and return a TermList from the given SEXP. 
   * @param argument the SEXP to interpret
   * @param subtracting true if we are subtracting and the intercept should be interpreted as 
   *        negative
   * @return
   */
  private TermList buildTermList(SEXP argument, boolean subtracting) {
    TermList list = new TermList();
    add(list, argument, subtracting);
    return list;
  }
  
  private TermList buildTermList(SEXP argument) {
    return buildTermList(argument, false);
  }

  private void add(TermList list, SEXP argument, boolean subtracting) {
    if(argument instanceof Symbol) {
      list.add(new Term(argument));
    } else if(argument instanceof Vector) {
      intercept((Vector)argument, subtracting);
    } else if(argument instanceof FunctionCall) {
      FunctionCall call = (FunctionCall)argument;
      if(call.getFunction() == UNION) {
        unionTerms(list, call);
      } else if(call.getFunction() == EXPAND_TERMS) {
        multiply(list, call);
      } else if(call.getFunction() == DIFFERENCE) {
        difference(list, call);
      } else if(call.getFunction() == GROUP) {
        add(list, call.getArgument(0), subtracting);
      } else {
        list.add(new TermBuilder().build(call));
      }
    }
  }
  
  private void add(TermList list, SEXP argument) {
    add(list, argument, false);
  }
  
  private void multiply(TermList terms, FunctionCall call) {
    TermList a = buildTermList(call.getArgument(0));
    TermList b = buildTermList(call.getArgument(1));
    
    terms.add(a);
    terms.add(b);
    
    for(Term a_i : a) {
      for(Term b_i : b) {
        terms.add(new Term(a_i, b_i));
      }
    }
  }

  private void unionTerms(TermList terms, FunctionCall call) {
    for(SEXP argument : call.getArguments().values()) {
      add(terms, argument);
    }
  }
  
  private void difference(TermList terms, FunctionCall call) {
    
    if(call.getArguments().length() == 1) {
      // the difference between an empty set and any other set is the empty set,
      // so we don't add any terms to our parent list, but we do
      // need to look for a negative intercept
      buildTermList(call.getArgument(0), true);
      
    } else {
      TermList a = buildTermList(call.getArgument(0));
      TermList b = buildTermList(call.getArgument(1), true);
      
      a.subtract(b);
      
      terms.add(a);
    }
  }

  private void intercept(Vector vector, boolean subtracting) {
  
    if(vector.length() != 1) {
      throw new EvalException("Invalid intercept: " + vector.toString() + ", expected 0 or 1");
    }
    intercept = vector.getElementAsInt(0);
    if(intercept != 0 && intercept != 1) {
      throw new EvalException("Invalid intercept: " + intercept + ", expected 0 or 1");
    }
    if(subtracting) {
      intercept = (intercept == 0) ? 1 : 0;
    } 
  }
}
