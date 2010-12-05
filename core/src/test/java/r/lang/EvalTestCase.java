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

package r.lang;

import org.junit.Before;
import r.parser.ParseOptions;
import r.parser.ParseState;
import r.parser.RLexer;
import r.parser.RParser;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public abstract class EvalTestCase {

  protected Environment global;
  public static final SEXP NULL = NullExp.INSTANCE;
  public static final SEXP CHARACTER_0 = new StringVector();
  public static final SEXP DOUBLE_0 = new DoubleVector();

  @Before
  public void setUp() {
    global = Environment.createGlobalEnvironment();
  }

  protected SEXP eval(String source) {
    return evaluate(source).getExpression();
  }

  protected EvalResult evaluate(String source)  {
    if(!source.endsWith(";") && !source.endsWith("\n")) {
      source = source + "\n";
    }
    SEXP exp = parse(source);
    return exp.evaluate(global);
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

  protected SEXP symbol(String name) {
    return new SymbolExp(name);
  }
}
