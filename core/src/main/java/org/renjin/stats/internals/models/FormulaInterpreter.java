package org.renjin.stats.internals.models;

import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Model formulas in R are defined with a Domain Specific Language (DSL)
 * that describe a {@link Formula}. The DSL uses the same Abstract Syntax
 * Tree (AST) as R expressions, but certain functions are interpreted differently 
 * in this context.
 * 
 */
public class FormulaInterpreter {
  
  private static final Symbol TILDE = Symbol.get("~");
  private static final Symbol UNION = Symbol.get("+");
  private static final Symbol EXPAND_TERMS = Symbol.get("*");
  private static final Symbol DIFFERENCE = Symbol.get("-");
  private static final Symbol GROUP = Symbol.get("(");
  private static final Symbol DOT = Symbol.get(".");

  private SEXP response;
  
  private int intercept = 1;

  private ListVector dataFrame = null;
  private boolean allowDotAsName = false;


  public Formula interpret(FunctionCall call) {
    if(call.getFunction() != TILDE) {
      throw new EvalException("expected model formula (~)");
    }
    
    FunctionCall expandedFormula;
    SEXP predictor;
    
    if(call.getArguments().length() == 1) {
      response = null;
      predictor = expandPredictor(call.getArgument(0), null);
      expandedFormula = FunctionCall.newCall(TILDE, predictor);
      
    } else if(call.getArguments().length() == 2) {
      response = call.getArgument(0);
      predictor = expandPredictor(call.getArgument(1), null);
      expandedFormula = FunctionCall.newCall(TILDE, response, predictor);
      
    } else {
      throw new EvalException("Expected at most two arguments to `~` operator");
    }

    TermList terms = new TermList();
    add(terms, predictor);
    
    return new Formula(expandedFormula, intercept, terms.sorted());
  }

  private SEXP expandPredictor(SEXP argument, SEXP parent) {
    if(!allowDotAsName && argument == DOT) {
      return expandRemainingVariables(parent);
        
    } else if(argument instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) argument;
      FunctionCall.Builder expandedCall = new FunctionCall.Builder();
      expandedCall.add(call.getFunction());
      for (PairList.Node node : call.getArguments().nodes()) {
        expandedCall.add(node.getName(), expandPredictor(node.getValue(), call));
      }
      return expandedCall.build();
      
    } else {
      return argument;
    }
  }

  private SEXP expandRemainingVariables(SEXP parent) {
    if(dataFrame == null) {
      throw new EvalException("'.' in formula and no 'data' argument");
    }

    Set<String> responseVariables = new HashSet<>();
    findResponseVariables(responseVariables, response);
    
    List<String> remainingVariables = Lists.newArrayList();

    for (int i = 0; i < dataFrame.length(); i++) {
      String variable = dataFrame.getName(i);
      if(!responseVariables.contains(variable)) {
        remainingVariables.add(variable);
      }
    }
    
    if(remainingVariables.isEmpty()) {
      return DOT;
    } else { 
      
      // In the context of x + .
      // If there are multiple variables, group with parens: x + (y + z)
      if(parent != null && remainingVariables.size() > 1) {
        return FunctionCall.newCall(GROUP, expandRemainingVariables(remainingVariables));
      } 
      // If we are not nested, or have a single variable, no grouping neccessary
      return expandRemainingVariables(remainingVariables);
    }
  }
  
  private SEXP expandRemainingVariables(List<String> remainingVariables) {

    Iterator<String> it = remainingVariables.iterator();
    SEXP expansion = Symbol.get(it.next());
    
    while(it.hasNext()) {
      expansion = FunctionCall.newCall(UNION, expansion, Symbol.get(it.next()));
    }
    
    return expansion;
  }

  private void findResponseVariables(Set<String> set, SEXP responseSexp) {
    if(responseSexp instanceof Symbol) {
      set.add(((Symbol) responseSexp).getPrintName());
    } else if(responseSexp instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) responseSexp;
      for (SEXP argument : call.getArguments().values()) {
        findResponseVariables(set, argument);
      }
    }
  }

  /**
   * Build and return a TermList from the given SEXP. 
   * @param argument the SEXP to interpret
   * @param subtracting true if we are subtracting and the intercept should be interpreted as 
   *        negative
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


  public FormulaInterpreter withData(SEXP data) {
    // GNU R ignores invalid data objects...
    if(data instanceof ListVector) {
      dataFrame = (ListVector) data; 
    }
    return this;
  }

  public FormulaInterpreter allowDotAsName(boolean allowDotAsName) {
    this.allowDotAsName = allowDotAsName;
    return this;
  }
}
