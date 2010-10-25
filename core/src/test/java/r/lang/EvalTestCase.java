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

  protected GlobalContext context;

  @Before
  public void setUp() {
    context = new GlobalContext();
  }

  protected SEXP evaluateToExpression(String source) throws IOException {
    return evaluate(source).getExpression();
  }

  protected EvalResult evaluate(String source) throws IOException {
    if(!source.endsWith(";") && !source.endsWith("\n")) {
      source = source + "\n";
    }
    SEXP exp = parse(source);
    return exp.evaluate(context.getGlobalEnvironment());
  }



  private SEXP parse(String source) throws IOException {
    ParseState state = new ParseState();
    ParseOptions options = ParseOptions.defaults();
    RLexer lexer = new RLexer(context, options, state, new StringReader(source));
    RParser parser = new RParser(options, state, context, lexer);

    assertThat("parser.parse succeeds", parser.parse(), equalTo(true));
    RParser.StatusResult status = parser.getResultStatus();
    return parser.getResult();
  }

}
