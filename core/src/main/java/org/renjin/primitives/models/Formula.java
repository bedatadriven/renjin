package org.renjin.primitives.models;

import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.renjin.primitives.matrix.IntMatrixBuilder;
import org.renjin.sexp.*;

import java.util.List;

/**
 * Encapsulates the properties of R model formulae, which consist of three elements:
 * 
 * <ul>
 * <li>A response (or dependent) <i>variable</i></li>
 * <li>An intercept flag (0=do not fit intercept, 1=fit intercept)</li>
 * <li>A list of independent variable <i>terms</i>
 * </ul>
 * 
 * <p>Within the context of R model formulas, a <i>variable</i> is any valid R
 * expression that can be evaluated within the formula's {@link Environment} (usually a
 * data.frame object). It is often a symbol, like simply {@code x} or {@code y}, but a 
 * <i>variable</i> in this context a <i>variable</i> can also be a function of a Symbol, 
 * such as {@code log(x)} or any valid R expression, such {@code sqrt(x/y)}.
 * 
 * <p>A <i>term</i> is a combination of one or more <i>variables</i>. This class stores terms in
 * their expanded form: the formula described by {@code y ~ a * b} actually has three terms: 
 * {@code a}, {@code b}, and {@code a:b}, where {@code a:b} is an interaction term. See 
 * {@link FormulaInterpreter} for more information on the DSL.
 * 
 * <p>This internal class is used by the primitives that manipulate model formulas. 
 */
public class Formula {
  private SEXP response;
  private List<Term> terms = Lists.newArrayList();
  private int intercept = 1;
  
  public Formula(SEXP response, int intercept, Iterable<Term> terms) {
    super();
    this.response = response;
    this.terms = Lists.newArrayList(terms);
    this.intercept = intercept;
  }
  
  public Formula(List<Term> terms) {
    this.terms = terms;
  }
  
  public SEXP getResponse() {
    return response;
  }
  public List<Term> getTerms() {
    return terms;
  }
  
  /**
   * 
   * @return
   */
  public int getIntercept() {
    return intercept;
  }
  
  /**
   * 
   * @return a list of all the unique <i>variables</i> that are referenced in this 
   * model formula.
   */
  public List<SEXP> uniqueVariables() {
    List<SEXP> variables = Lists.newArrayList();
    
    if(response != null) {
      variables.add(response);
    }
    
    for(Term term : terms) {
      for(SEXP expr : term) {
        if(!variables.contains(expr)) {
          variables.add(expr);
        }
      }
    }
    return variables;
  }
  
  /**
   * 
   * @return An unevaluated {@link FunctionCall} to ‘list’ of the variables in the model. 
   * This is provided in unevaluated form so it can later be conveniently evaluated within
   * the formula's environment.
   */
  public FunctionCall buildVariablesAttribute() {
    PairList.Builder args = new PairList.Builder();
    for(SEXP variable : uniqueVariables()) {
      args.add(variable);
    }
    return new FunctionCall(Symbol.get("list"), args.build());
  }


  /**
   * @return A matrix of variables by terms showing which variables appear
   * in which terms.  The entries are 0 if the variable does not
   * occur in the term, 1 if it does occur and should be coded by
   * contrasts, and 2 if it occurs and should be coded via dummy
   * variables for all levels (as when an intercept or lower-order
   * term is missing).  If there are no terms other than an
   * intercept and offsets, this is ‘numeric(0)’.
   */
  public SEXP buildFactorsMatrix() {
    if(terms.size() == 0) {
      return new IntArrayVector();
    } else {
      List<SEXP> variables = uniqueVariables();
      IntMatrixBuilder matrix = new IntMatrixBuilder(variables.size(), terms.size());
      matrix.setRowNames(Collections2.transform(variables, Functions.toStringFunction()));
      matrix.setColNames(buildTermLabels());
      for(int row = 0; row != variables.size(); ++row) {
        for(int col = 0; col != terms.size(); ++col) {
          if(terms.get(col).getExpressions().contains(
              variables.get(row))) {
            // TODO(alex): I don't know when this should be 1 or 2??
            matrix.set(row, col, 1);
          } else {
            matrix.set(row, col, 0);
          }
        }
      }
      return matrix.build();
    }
  }
  
  /**
   * @return A character vector containing the labels for each of the
   * terms in the model, except for offsets.  Non-syntactic names
   * will be quoted by backticks.  Note that these are after
   * possible re-ordering (unless argument ‘keep.order’ was
   * false).
   */
  public StringVector buildTermLabels() {
    StringVector.Builder labels = new StringVector.Builder();
    for(Term term : terms) {
      labels.add(term.getLabel());
    }
    return labels.build();
  }
  
  /**
   * @return Either 0, indicating no intercept is to be fit, or 1
   *                indicating that an intercept is to be fit.
   */
  public IntVector buildInterceptAttribute() {
    return new IntArrayVector(intercept);
  }
  
  /**
   * @return  The index of the variable (in variables) of the response (the
   * left hand side of the formula). Zero, if there is no
   * response.
   */
  public IntVector buildResponseAttribute() {
    return new IntArrayVector(response == null ? 0 : 1 );
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + intercept;
    result = prime * result + ((response == null) ? 0 : response.hashCode());
    result = prime * result + ((terms == null) ? 0 : terms.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Formula other = (Formula) obj;
    if (intercept != other.intercept) {
      return false;
    }
    if (response != other.response) {
        return false;
    }
    return terms.equals(other.terms);
  }

  @Override
  public String toString() {
    return response + " ~ " + intercept + " + " + terms.toString();
  }
}
