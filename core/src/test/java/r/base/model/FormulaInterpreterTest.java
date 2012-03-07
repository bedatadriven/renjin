package r.base.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.renjin.primitives.models.Formula;
import org.renjin.primitives.models.FormulaInterpreter;
import org.renjin.primitives.models.Term;

import r.lang.ExpressionVector;
import r.lang.FunctionCall;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.exception.EvalException;
import r.parser.RParser;

import com.google.common.collect.Lists;

public class FormulaInterpreterTest {

  @Test
  public void simple() {
    
    assertThat(build("y ~ x"), equalTo(formula("y", terms("x"))));
    assertThat(build("y ~ x + y"), equalTo(formula("y", terms("x", "y"))));
    assertThat(build("y ~ x + y + x:y"), equalTo(formula("y", terms("x", "y", interaction("x", "y"))))); 
  }

  @Test
  public void withArithmatic() {
    assertThat(build("y ~ log(x)"), equalTo(formula("y", terms("log(x)"))));
    assertThat(build("y ~ I(x+1)"), equalTo(formula("y", terms("I(x+1)"))));
  }

  @Test
  public void expansion() {
    assertThat(build("y ~ a * b"), equalTo(formula("y", terms("a","b", interaction("a","b")))));
    assertThat(build("y ~ a:c * b"), equalTo(formula("y", terms("b",interaction("a","c"), interaction("a","c","b")))));
  }
  
  @Test
  public void groupedExpansion() {
    assertThat(build("y ~ (a+b) * c"), equalTo(formula("y", terms("a","b", "c", interaction("a","c"), interaction("b","c")))));
    assertThat(build("y ~ (a+b) * (c+d)"), equalTo(formula("y", 
        terms("a","b", "c", "d", 
             interaction("a","c"), 
             interaction("a","d"),
             interaction("b","c"),
             interaction("b","d")))));
  }
  
  @Test
  public void removeIntercept() {
    assertThat(build("y ~ x - 1"), equalTo(formula("y", 0, terms("x"))));
    assertThat(build("y ~ 1 - 1"), equalTo(formula("y", 0, terms())));
    assertThat(build("y ~ (-1)"), equalTo(formula("y", 0, terms())));
    assertThat(build("y ~ 1 - 1 + 1"), equalTo(formula("y", 1, terms())));

  }

  @Test
  public void substractingTerms() {
    assertThat(build("y ~ a * b - b"), equalTo(formula("y", terms("a", interaction("a","b")))));
  }
  
  @Test(expected=EvalException.class)
  public void invalidIntercept() {
    build("y ~ 6");
  }
  
  private Formula build(String source) {
    SEXP expr = parse(source);
    return new FormulaInterpreter().interpret((FunctionCall) expr);
  }

  private SEXP parse(String source) {
    ExpressionVector tree = RParser.parseSource(source + "\n");
    SEXP expr = tree.getElementAsSEXP(0);
    return expr;
  }
  
  private Formula formula(String response, List<Term> terms) {
    return new Formula(Symbol.get(response), 1, terms);
  }
  
  private Formula formula(String response, int intercept, List<Term> terms) {
    return new Formula(Symbol.get(response), intercept, terms);
  }
  
  
  private List<Term> terms(Object...terms) {
    List<Term> list = Lists.newArrayList();
    for(Object term : terms) {
      if(term instanceof String) {
        list.add(new Term(parse((String)term)));
      } else if(term instanceof Term) {
        list.add((Term)term);
      } else {
        throw new IllegalArgumentException(term.toString());
      }
    }
    return list;
  }
  
  private Term interaction(String... variableNames) {
    List<SEXP> variables = Lists.newArrayList();
    for(String name : variableNames) {
      variables.add(parse(name));
    }
    return new Term(variables);
  }  
}
