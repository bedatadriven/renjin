/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
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

package r.base;

import org.junit.Test;
import r.EvalTestCase;
import r.lang.SEXP;
import r.lang.StringVector;
import r.parser.RParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BasePackageTest extends EvalTestCase {


  @Test
  public void loadBase() throws IOException {

    loadBasePackage();

    StringVector letters = (StringVector) eval("letters");
    assertThat( letters.getElement(0),  equalTo( "a" ));
    assertThat( letters.getElement(25), equalTo( "z" ));

    eval( "assign('x', 42) ");
    assertThat( eval( "x" ) , equalTo( c(42) ));
  }

  @Test
  public void libPaths() throws Exception {

    loadBasePackage();
    executeStartupProfile();

    // This is a pretty complicated evaluation here that involves recursive
    // lazy loading, persisted environments, local environments, etc.
    // So a good test that everything integrates together!
    assertThat(eval(".libPaths() "), equalTo(c("res:r/library")));
  }


  @Test
  public void packageVersion() throws IOException {
    loadBasePackage();
    executeStartupProfile();

    eval(" x <- package_version('1.2-4') ");


  }



  @Test
  public void groupGeneric() throws IOException {
    loadBasePackage();
    executeStartupProfile();

    eval(" x <- as.numeric_version('1.2.3') ");
    eval(" y <- as.numeric_version('1.0.9') ");

    assertThat( eval(" x >= y"), equalTo( c(true)));


  }

  @Test
  public void fileInfo() throws IOException {

    loadBasePackage();

    eval("info <- file.info('res:r/library')");

    assertThat( eval("info$isdir"), equalTo( c(true) ));
    assertThat( eval("info$mode"), equalTo( c_i(Integer.parseInt("777", 8)) ));
  }

  @Test
  public void library() throws Exception {
    loadBasePackage();
    executeStartupProfile();

    java.lang.System.out.println(eval(".find.package('survey') "));
    eval(" library(survey) ");
  }

  @Test
  public void parse() throws IOException {
    loadBasePackage();
    executeStartupProfile();

    assertThat( eval(" parse(text='1') "), equalTo(expression(1d)));

  }

  private void loadBasePackage() throws IOException {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/r/library/base/R/base"));
    SEXP loadingScript = RParser.parseSource(reader).evaluate(topLevelContext, base).getExpression();
    loadingScript.evaluate(topLevelContext, base);
  }

  private void executeStartupProfile() throws IOException {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/r/library/base/R/Rprofile"));
    SEXP profileScript = RParser.parseSource(reader).evalToExp(topLevelContext, base);
    profileScript.evaluate(topLevelContext, base);
  }
}
