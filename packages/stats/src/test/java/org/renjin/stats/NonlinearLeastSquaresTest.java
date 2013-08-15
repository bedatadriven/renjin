package org.renjin.stats;

import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.parser.RParser;
import org.renjin.sexp.*;
import org.renjin.stats.nls.NonlinearLeastSquares;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class NonlinearLeastSquaresTest extends EvalTestCase{

  @Test
  public void basicTest() {
    eval("x<-1:10");
    eval("y<-2*x^2 + 3*x + 4 + rnorm(10)");
    eval("df <- data.frame(x = x, y = y)");
    eval("formula <- y ~ a*x^2 + b*x + cc");
    try {
      eval("model <- nls(formula, df, trace = T)");
    } catch(EvalException e) {
      System.err.println("ERROR: " + e.getMessage());
      e.printRStackTrace(System.err);
      
    }
    printWarnings();
    
  }

  
  @Test
  public void numericDerivative() throws IOException {
    
    Environment rho = Environment.createChildEnvironment(context.getGlobalEnvironment());
    rho.setVariable("x", new DoubleArrayVector(1,2,3));
    rho.setVariable("a", new DoubleArrayVector(1));
    rho.setVariable("b", new DoubleArrayVector(1));
    rho.setVariable("cc", new DoubleArrayVector(1));

    // f(x) = a*x^2 + b + cc
    ExpressionVector modelExpr = RParser.parseAllSource(new StringReader("a*x^2 + b*x + cc"));

    StringVector theta = new StringArrayVector("a","b","cc");
    DoubleArrayVector dir = new DoubleArrayVector(1,1,1);

    SEXP result = NonlinearLeastSquares.numericDerivative(context, modelExpr, theta, rho, dir);
    
    DoubleVector gradient = (DoubleVector) result.getAttribute(Symbol.get("gradient"));
    SEXP gradientDim = gradient.getAttributes().getDim();
    assertThat(gradientDim, equalTo(c_i(3, 3)));

    double allowableError = 0.0000001;


    // f'(a) = x^2
    assertThat(gradient.getElementAsDouble(0), closeTo(1, allowableError));
    assertThat(gradient.getElementAsDouble(1), closeTo(4, allowableError));
    assertThat(gradient.getElementAsDouble(2), closeTo(9, allowableError));

    // f'(b) = x
    assertThat(gradient.getElementAsDouble(3), closeTo(1, allowableError));
    assertThat(gradient.getElementAsDouble(4), closeTo(2, allowableError));
    assertThat(gradient.getElementAsDouble(5), closeTo(3, allowableError));

    // f'(c) = 1
    assertThat(gradient.getElementAsDouble(6), closeTo(1, allowableError));
    assertThat(gradient.getElementAsDouble(7), closeTo(1, allowableError));
    assertThat(gradient.getElementAsDouble(8), closeTo(1, allowableError));
  }
}

