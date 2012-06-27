package org.renjin.primitives.models;

import com.google.common.collect.Lists;
import org.renjin.eval.EvalException;
import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Encapsulates an R 'terms' object.
 * 
 * <p>A 'terms' object is an alternative, more explicit representation
 * of a formula object. 
 *
 */
public class TermsObject {

  public static final Symbol INTERCEPT = Symbol.get("intercept");
  public static final Symbol RESPONSE = Symbol.get("response");
  public static final Symbol FACTORS = Symbol.get("factors");
  
  private final SEXP object;
  private final int intercept;
  private final int response;
  private final Matrix factorMatrix;
  private final StringVector variableNames;
  
  private final List<Term> terms = Lists.newArrayList();

  public TermsObject(SEXP object) {
    this.object = object;
    this.intercept = getIntAttribute(INTERCEPT);
    this.response = getIntAttribute(RESPONSE);
    try {
      this.factorMatrix = new Matrix((Vector)object.getAttribute(FACTORS));
      this.variableNames = (StringVector) factorMatrix.getRowNames();
    } catch(Exception e) {
      throw new EvalException("invalid 'factors' attribute");
    }
    
    for(int i=0;i!=getNumTerms();++i) {
      terms.add(new Term(i));
    }
  }
  
  private int getIntAttribute(Symbol attributeName) {
    SEXP exp = object.getAttribute(attributeName);
    if(!(exp instanceof IntVector) || exp.length() != 1) {
      throw new EvalException("invalid attribute '" + attributeName + "'");
    }
    return ((Vector)exp).getElementAsInt(0);
  }

  public int getIntercept() {
    return intercept;
  }

  public int getResponse() {
    return response;
  }
 
  public int getNumTerms() {
    return factorMatrix.getNumCols();
  }
  
  public int getNumVariables() {
    return factorMatrix.getNumRows();
  }
  
  public StringVector getVariableNames() {
    return variableNames;
  }

  public boolean hasIntercept() {
    return intercept == 1;
  }
  
  public Term getTerm(int index) {
    return terms.get(index);
  }
  
  
  public class Term {
    
    private int termIndex;
    
    public Term(int termIndex) {
      super();
      this.termIndex = termIndex;
    }

    public boolean containsVariable(int variableIndex) {
      return factorMatrix.getElementAsInt(variableIndex, termIndex) != 0;
    }
    
    public int getContrastType(int variableIndex) {
      return factorMatrix.getElementAsInt(variableIndex, termIndex);
    }
    
    public int getTermIndex() {
      return termIndex;
    }
    
    public boolean isResponse() {
      return response > 0 && containsVariable(response-1) && size() == 1;
    }
  
    /**
     * @return the number of variables in this Term
     */
    public int size() {
      int count=0;
      for(int varIndex=0;varIndex!=getNumVariables();++varIndex) {
        if(containsVariable(varIndex)) {
          count++;
        }
      }
      return count;
    }
    
    public Iterable<Integer> variableIndexes() {
      List<Integer> indices = Lists.newArrayList();
      for(int i=0;i!=getNumVariables();++i) {
        if(containsVariable(i)) {
          indices.add(i);
        }
      }
      return indices;
    }
  }
  
  public Iterable<Term> getTerms() {
    return terms;
  }
}
