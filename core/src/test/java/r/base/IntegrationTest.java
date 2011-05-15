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

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import r.EvalTestCase;
import r.lang.*;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Collection of largish tests to make sure everything is playing
 * correctly together.
 */
public class IntegrationTest extends EvalTestCase {


  @Test
  public void loadBase() throws IOException {

    loadBasePackage();
    executeStartupProfile();

    StringVector letters = (StringVector) eval("letters");
    assertThat( letters.getElement(0),  equalTo( "a" ));
    assertThat( letters.getElement(25), equalTo( "z" ));

    eval( "assign('x', 42) ");
    assertThat( eval( "x" ) , equalTo( c(42) ));

    // make sure that closures are enclosed by the base namspace
    Closure closure = (Closure)getValue( topLevelContext.getGlobals().baseEnvironment, "backsolve" );
    assertThat( closure.getEnclosingEnvironment(), equalTo(topLevelContext.getGlobals().baseNamespaceEnv ));


    // make sure that base scripts are populated in both the base environment and the base namespace
    assertThat( getValue( topLevelContext.getGlobals().baseEnvironment, "letters" ).length(), equalTo( 26 ));
  //  assertThat( getValue( topLevelContext.getGlobals().baseNamespaceEnv, "letters" ).length(), equalTo( 26 ));

  }

  private SEXP getValue(Environment env, String name) {
    SEXP value = env.getVariable(name);
    if(value instanceof Promise) {
      value = ((Promise) value).force().getExpression();
    }
    return value;
  }

  @Ignore
  @Test
  public void libPaths() throws Exception {

    loadBasePackage();
    executeStartupProfile();

    // This is a pretty complicated evaluation here that involves recursive
    // lazy loading, persisted environments, local environments, etc.
    // So a good test that everything integrates together!
    assertThat(eval(".libPaths() "), equalTo(c("classpath:/r/library")));
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

    assertThat(eval(" x >= y"), equalTo(c(true)));
  }

  @Test
  public void fileInfo() throws IOException {

    loadBasePackage();

    eval("info <- file.info('" + getClass().getResource("/r/library/base/R/base").getFile() + "')");

    assertThat(eval("info$isdir"), equalTo(c(false)));
    assertThat(eval("info$mode"), equalTo(c_i(Integer.parseInt("666", 8))));
  }

  @Test
  public void dquote() throws IOException {

    loadBasePackage();
    executeStartupProfile();

    assertThat( eval(" dQuote('a') "), equalTo( c("\"a\"")) );
  }


  @Test
  public void formals() throws IOException {

    loadBasePackage();
    executeStartupProfile();

    eval("g <- function() sys.parent() ");
    eval("f <- function() g() ");

    assertThat( eval("f()"), equalTo(c_i(1)));

    eval("g<-function() eval(formals(sys.function(sys.parent()))[['event']]) ");
    eval("f<-function(event=c('a','b','c')) g() ");

    SEXP result = eval("f(1) ");
    assertThat(result, Matchers.equalTo(c("a", "b", "c")));
  }

  @Test
  public void lapply() throws Exception {
    loadBasePackage();
    executeStartupProfile();

    eval("f<-function(a,b) a+b ");
    eval("x<-c(1)");
    assertThat( eval("lapply(x,f,2) "), equalTo(list(3d)));
  }

  @Test
  public void packages() throws Exception {
    topLevelContext.init();

    java.lang.System.out.println(eval(".packages()"));
  }

  @Test
  public void surveyPackage() throws Exception {
    topLevelContext.init();

    java.lang.System.out.println(eval(".find.package('survey') "));
    eval(" library(survey) ");

    assertThat( eval(" data(hospital, verbose=TRUE) "), equalTo(c("hospital")) );

    java.lang.System.out.println( eval("ls() "));

    assertThat(eval("sum(hospital$births)"), equalTo(c(25667)));

//    eval("dstr <- svydesign(id = ~1, strata = ~oblevel, fpc = ~tothosp, weight = ~weighta, data = hospital)");
//    eval("svymean(~births, dstr)");

  }

  @Test
  public void parse() throws IOException {
    loadBasePackage();
    executeStartupProfile();

    assertThat(eval(" parse(text='1') "), equalTo(expression(1d)));

  }

  private void loadBasePackage() throws IOException {
    topLevelContext.loadBasePackage();
  }

  private void executeStartupProfile() throws IOException {
    topLevelContext.executeStartupProfile();
  }

}
