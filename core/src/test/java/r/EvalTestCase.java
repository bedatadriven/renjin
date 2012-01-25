/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.math.complex.Complex;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;

import r.compiler.ir.tac.IRFunctionTable;
import r.lang.ComplexVector;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.ExpressionVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Logical;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.Raw;
import r.lang.RawVector;
import r.lang.SEXP;
import r.lang.SEXPFactory;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Vector;
import r.parser.ParseOptions;
import r.parser.ParseState;
import r.parser.RLexer;
import r.parser.RParser;

import com.google.common.collect.Lists;

public abstract class EvalTestCase {

  
  protected Environment global;
  protected Environment base;
  protected Context topLevelContext;
  public static final SEXP NULL = Null.INSTANCE;
  public static final SEXP CHARACTER_0 = new StringVector();
  public static final SEXP DOUBLE_0 = new DoubleVector();

  public SEXP GlobalEnv;
  
  @Before
  public final void setUp() {
    topLevelContext = Context.newTopLevelContext();
    global = topLevelContext.getEnvironment();
    base = global.getBaseEnvironment();
    GlobalEnv = global;
  }

  protected SEXP eval(String source) {
    return evaluate(source);
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
    return new ComplexVector(values);
  }

  protected SEXP c(boolean... values) {
    return new LogicalVector(values);
  }

  protected SEXP c(Logical... values) {
    return new LogicalVector(values);
  }

  protected SEXP c(String... values) {
    return new StringVector(values);
  }

  protected SEXP c(double... values) {
    return new DoubleVector(values);
  }
  
  protected SEXP c(Raw... values){
    return new RawVector(values);
  }

  protected SEXP c_i(int... values) {
    return new IntVector(values);
  }

  protected SEXP list(Object... values) {
    ListVector.Builder builder = ListVector.newBuilder();
    for(Object obj : values) {
      builder.add(SEXPFactory.fromJava(obj));
    }
    return builder.build();
  }

  protected SEXP expression(Object... values) {
    List<SEXP> builder = Lists.newArrayList();
    for(Object obj : values) {
      builder.add(SEXPFactory.fromJava(obj));
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
            if(Math.abs(delta) > epsilon) {
              return false;
            }
          }
        }
        return true;
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
    DoubleVector.Builder matrix = new DoubleVector.Builder();
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
    ComplexVector.Builder matrix = new ComplexVector.Builder();
    int nrows = rows.length;
    int ncols = rows[0].length;
    
    for(int j=0;j!=ncols;++j) {
      for(int i=0;i!=nrows;++i) {
        matrix.add(rows[i][j]);
      }
    }
    return matrix.build();
  }
}
