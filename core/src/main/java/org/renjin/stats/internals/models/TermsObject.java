package org.renjin.stats.internals.models;

import com.google.common.base.Joiner;
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
  
  private final ModelFrame frame;
  private final List<Term> terms = Lists.newArrayList();

  public TermsObject(SEXP object, ModelFrame frame) {
    this.object = object;
    this.frame = frame;
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
 
  public Iterable<Term> getTerms() {
    return terms;
  }
  
  private int getIntAttribute(Symbol attributeName) {
    SEXP exp = object.getAttribute(attributeName);
    if(!(exp instanceof AtomicVector) || exp.length() != 1) {
      throw new EvalException("invalid attribute '" + attributeName + "'");
    }
    return ((AtomicVector)exp).getElementAsInt(0);
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
  
  public String toString() {
    return Joiner.on(" + ").join(terms);
  }
  
  
  /**
   * A term is a single variable, or a single interaction between
   * two or more variables that is a member of a model. 
   * 
   * <p>A variable, in turn, need not be univariate, they can 
   * also be multivariate. In this case, they will also occupy
   * multiple columns in a model matrix.</p>
   * 
   * <p>If one or more of the terms is a factor, the term will be
   * expanded into several columns within the model matrix.</p>
   * 
   * <p>Examples of terms:
   * <ul>
   * <li>x</li>
   * <li>x:y (actually expressed as x*y within the modeling DSL)</li>
   * </ul>
   * 
   */
  public class Term {
    
    private int termIndex;
    private List<Variable> variables = Lists.newArrayList();
    
    public Term(int termIndex) {
      super();
      this.termIndex = termIndex;
      
      for(int i=0;i!=getNumVariables();++i) {
        if(containsVariable(i)) {
          variables.add(frame.getVariable(i));
        }
      }
    }
    
    public List<? extends ModelMatrixColumn> getModelMatrixColumns() {
      if(variables.size() == 1) {
        return variables.get(0).getModelMatrixColumns();
      } else {
                
        // find the maximum number of columns
        int numColumns = 1;
        for(Variable variable : variables) {
          List<? extends ModelMatrixColumn> columns = variable.getModelMatrixColumns();
          if(columns.size() > numColumns) {
            numColumns = columns.size();
          }
        }
       
        List<ModelMatrixColumn> termColumns = Lists.newArrayList();
        for(int i=0;i!=numColumns;++i) {
          List<ModelMatrixColumn> parts = Lists.newArrayList();
          for(Variable variable : variables) {
            List<? extends ModelMatrixColumn> variableColumns = variable.getModelMatrixColumns();
            parts.add(variableColumns.get( i % variableColumns.size() ));
          }
          termColumns.add(new InteractionMatrixColumn(parts));
        }
        
        return termColumns;
      }
    }
    
    public List<Variable> getVariables() {
      return variables;
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
    
    public String toString() {
      return Joiner.on(":").join(variables);
    }
  }
  
}
