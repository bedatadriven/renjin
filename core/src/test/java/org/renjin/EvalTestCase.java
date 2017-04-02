/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin;

import org.apache.commons.math.complex.Complex;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;
import org.renjin.eval.Context;
import org.renjin.invoke.reflection.converters.Converters;
import org.renjin.parser.ParseOptions;
import org.renjin.parser.ParseState;
import org.renjin.parser.RLexer;
import org.renjin.parser.RParser;
import org.renjin.primitives.Warning;
import org.renjin.primitives.io.connections.ResourceConnection;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.primitives.UnsignedBytes;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public abstract class EvalTestCase {


  protected Environment global;
  protected Environment base;
  protected Context topLevelContext;
  public static final SEXP NULL = Null.INSTANCE;
  public static final SEXP CHARACTER_0 = new StringArrayVector();
  public static final SEXP DOUBLE_0 = new DoubleArrayVector();

  public SEXP GlobalEnv;

  @Before
  public final void setUp() {
    topLevelContext = Context.newTopLevelContext();
    global = topLevelContext.getEnvironment();
    base = topLevelContext.getBaseEnvironment();
    GlobalEnv = global;
  }

  protected SEXP eval(String source) {
    SEXP result = evaluate(source);
    printWarnings();
    return result;
  }


  
  private void printWarnings() {
    SEXP warnings = topLevelContext.getBaseEnvironment().getVariable(Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
          topLevelContext.getBaseEnvironment());

      System.out.println();
    }
  }
    
  
  /**
   * Fully initializes the context, loading the R-language
   * base packages and recommended packages.
   * If this initializes fails, an AssumptionViolatedError exception 
   * will be thrown rather than an error.
   */
  protected void assumingBasePackagesLoad() {
    try {
      topLevelContext.init();
    } catch(Exception e) {
      throw new AssumptionViolatedException("Exception thrown while loading R-language packages");
    }
  }

  protected SEXP evaluate(String source)  {
    if(!source.endsWith(";") && !source.endsWith("\n")) {
      source = source + "\n";
    }
    SEXP exp = parse(source);


    return topLevelContext.evaluate( exp );
  }

  private SEXP parse(String source)  {
    try {
      ParseState state = new ParseState();
      state.setSrcFile(new CHARSEXP("inline-source"));
      ParseOptions options = ParseOptions.defaults();
      RLexer lexer = new RLexer(options, state, new StringReader(source));
      RParser parser = new RParser(options, state, lexer);
      assertThat("parser.parse succeeds", parser.parse(), equalTo(true));
      RParser.StatusResult status = parser.getResultStatus();
      return parser.getResult();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected Complex complex(double real) {
    return new Complex(real, 0);
  }

  protected Complex complex(double real, double imaginary) {
    return new Complex(real, imaginary);
  }

  protected SEXP c(Complex... values) {
    return new ComplexArrayVector(values);
  }

  protected SEXP c(boolean... values) {
    return new LogicalArrayVector(values);
  }

  protected SEXP c(Logical... values) {
    return new LogicalArrayVector(values);
  }

  protected SEXP c(String... values) {
    return new StringArrayVector(values);
  }

  protected SEXP c(double... values) {
    return new DoubleArrayVector(values);
  }

  protected SEXP c_raw(int... values){
    byte[] bytes = new byte[values.length];
    for(int i=0;i!=values.length;++i) {
      bytes[i] = UnsignedBytes.checkedCast(values[i]);
    }
    return new RawVector(bytes);
  }

  protected SEXP c_i(int... values) {
    return new IntArrayVector(values);
  }

  protected SEXP list(Object... values) {
    ListVector.Builder builder = ListVector.newBuilder();
    for(Object obj : values) {
      builder.add(Converters.fromJava(obj));
    }
    return builder.build();
  }

  protected SEXP expression(Object... values) {
    List<SEXP> builder = Lists.newArrayList();
    for(Object obj : values) {
      builder.add(Converters.fromJava(obj));
    }
    return new ExpressionVector(builder);
  }

  protected SEXP symbol(String name) {
    return Symbol.get(name);
  }

  protected Matcher<SEXP> closeTo(final SEXP expectedSexp, final double epsilon) {
    final Vector expected = (Vector)expectedSexp;
    return new TypeSafeMatcher<SEXP>() {

      @Override
      public void describeTo(Description d) {
        d.appendText(expectedSexp.toString());
      }

      @Override
      public boolean matchesSafely(SEXP item) {
        if(!(item instanceof Vector)) {
          return false;
        }
        Vector vector = (Vector)item;
        if(vector.length() != expected.length()) {
          return false;
        }
        for(int i=0;i!=expected.length();++i) {
          if(expected.isElementNA(i) != vector.isElementNA(i)) {
            return false;
          }
          if(!expected.isElementNA(i)) {
            double delta = expected.getElementAsDouble(i)-vector.getElementAsDouble(i);
            if(Double.isNaN(delta) || Math.abs(delta) > epsilon) {
              return false;
            }
          }
        }
        return true;
      }
    };
  }
  
  protected Matcher<SEXP> closeTo(final Complex expectedValue, final double epsilon) {
    return new TypeSafeMatcher<SEXP>() {
      @Override
      protected boolean matchesSafely(SEXP sexp) {
        if(!(sexp instanceof ComplexVector)) {
          return false;
        }
        if(sexp.length() != 1) {
          return false;
        }
        ComplexVector vector = (ComplexVector) sexp;
        Complex value = vector.getElementAsComplex(0);
        double realDelta = Math.abs(value.getReal() - expectedValue.getReal());
        if(Double.isNaN(realDelta) || realDelta > epsilon) {
          return false;
        }
        double imaginaryDelta = Math.abs(value.getImaginary() - expectedValue.getImaginary());
        if(Double.isNaN(imaginaryDelta) || imaginaryDelta > epsilon) {
          return false;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("complex value close to ").appendValue(expectedValue);
      }
    };
  }

  // otherwise this won't get resovled
  protected Matcher<Double> closeTo(double d, double epsilon) {
    return Matchers.closeTo(d, epsilon);
  }

  protected Matcher<Double> closeTo(int d, double epsilon) {
    return Matchers.closeTo((double)d, epsilon);
  }

  protected Complex[] row(Complex... z){
    return z;
  }

  protected double[] row(double... d) {
    return d;
  }

  protected SEXP matrix(double[]... rows) {
    DoubleArrayVector.Builder matrix = new DoubleArrayVector.Builder();
    int nrows = rows.length;
    int ncols = rows[0].length;

    for(int j=0;j!=ncols;++j) {
      for(int i=0;i!=nrows;++i) {
        matrix.add(rows[i][j]);
      }
    }
    return matrix.build();
  }
  protected SEXP matrix(Complex[]... rows) {
    ComplexArrayVector.Builder matrix = new ComplexArrayVector.Builder();
    int nrows = rows.length;
    int ncols = rows[0].length;

    for(int j=0;j!=ncols;++j) {
      for(int i=0;i!=nrows;++i) {
        matrix.add(rows[i][j]);
      }
    }
    return matrix.build();
  }

  protected Matcher<SEXP> elementsEqualTo(double... elements) {
    return elementsEqualTo(new DoubleArrayVector(elements));
  }

//
//  protected Matcher<SEXP> elementsEqualTo(int... elements) {
//    return elementsEqualTo(new IntArrayVector(elements));
//  }

  protected Matcher<SEXP> elementsEqualTo(final SEXP expected) {
    return new TypeSafeMatcher<SEXP>() {
      @Override
      public boolean matchesSafely(SEXP actual) {
        Vector v1 = (Vector)actual;
        Vector v2 = (Vector)expected;
        if(v1.length() != v2.length()) {
          return false;
        }
        for(int i=0;i!=v1.length();++i) {
          if(v1.isElementNA(i) != v2.isElementNA(i)) {
            return false;
          } else if(v1.getVectorType().compareElements(v1, i, v2, i) != 0) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendValue(expected.toString());
      }
    };
  }
  
  protected SEXP openResourceAsConn(String resourceName) {
    ResourceConnection conn = new ResourceConnection(getClass(), resourceName);
    IntVector connSexp = topLevelContext.getSession().getConnectionTable().newConnection(conn);
    return connSexp;
  }
  
  protected final String getString(String variableName) {
    SEXP sexp = topLevelContext.getGlobalEnvironment().getVariable(variableName);
    return ((StringVector) sexp).getElementAsString(0);
  }
}
