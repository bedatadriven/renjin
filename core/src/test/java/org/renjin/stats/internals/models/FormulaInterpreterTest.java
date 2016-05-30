package org.renjin.stats.internals.models;

import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
  
  @Test
  public void dotExpansion() {
    ListVector.NamedBuilder df = ListVector.newNamedBuilder();
    df.add("x", new DoubleArrayVector(1,2,3));
    df.add("y", new DoubleArrayVector(1, 2, 3));
    df.add("z", new DoubleArrayVector(1, 2, 3));

    assertThat(build(" x ~ .", df.build()).getExpandedFormula(), equalTo(parse("x ~ y + z")));
    assertThat(build(" x ~ y + .", df.build()).getExpandedFormula(), equalTo(parse("x ~ y + (y + z)")));
  }

  private Formula build(String source) {
    return build(source, Null.INSTANCE);
  }
  
  private Formula build(String source, SEXP dataFrame) {
    SEXP expr = parse(source);
    return new FormulaInterpreter()
        .withData(dataFrame)
        .interpret((FunctionCall) expr);
  }

  private SEXP parse(String source) {
    ExpressionVector tree = RParser.parseSource(source + "\n");
    SEXP expr = tree.getElementAsSEXP(0);
    return expr;
  }
  
  private Formula formula(String response, List<Term> terms) {
    return new Formula(FunctionCall.newCall(Symbol.get("~") ,Symbol.get(response), Null.INSTANCE), 1, terms);
  }
  
  private Formula formula(String response, int intercept, List<Term> terms) {
    return new Formula(FunctionCall.newCall(Symbol.get("~") ,Symbol.get(response), Null.INSTANCE), intercept, terms);
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
