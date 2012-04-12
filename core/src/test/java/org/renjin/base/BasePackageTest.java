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

package org.renjin.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.eval.Context;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;


/**
 * Tests that ensure the primitives integrate nicely with the 
 * R-language functions of the base package
 */
public class BasePackageTest extends EvalTestCase {


  @Test
  public void loadBase() throws IOException {

    topLevelContext.init();
    
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
      value = ((Promise) value).force();
    }
    return value;
  }

  @Test
  public void packageVersion() throws IOException {
    loadBasePackage();

    eval(" x <- package_version('1.2-4') ");
  }

  @Test
  public void groupGeneric() throws IOException {
    loadBasePackage();

    eval(" x <- as.numeric_version('1.2.3') ");
    eval(" y <- as.numeric_version('1.0.9') ");

    assertThat(eval(" x >= y"), equalTo(c(true)));
  }
  

  @Test
  public void versionCompare2() throws IOException {
    loadBasePackage();

    eval(" x <- as.numeric_version('2.10.1') ");
    eval(" y <- as.numeric_version('2.2.0') ");

    assertThat(eval(" x >= y"), equalTo(c(true)));
  }
  
  @Test
  public void rowNames() throws IOException {
    assumingBasePackagesLoad();
    
    eval(" xi <- list(c(55, 60, 30, 40, 11)) ");
    eval(" attr(xi, 'row.names') <- c(NA, -5) ");
    eval(" class(xi) <- 'data.frame' ");
    
    assertThat( eval("attr(xi, 'row.names')"), equalTo(c_i(1,2,3,4,5)));
    assertThat( eval("attributes(xi)$row.names"), equalTo(c_i(1,2,3,4,5)));
    assertThat( eval("row.names(xi) "), equalTo(c("1", "2", "3", "4", "5")));
  }

  @Test
  public void fileInfo() throws IOException {

    loadBasePackage();

    eval("info <- file.info('" + getClass().getResource("/org/renjin/library/base/R/base").getFile() + "')");

    assertThat(eval("info$isdir"), equalTo(c(false)));
    assertThat(eval("info$mode"), equalTo(c_i(Integer.parseInt("666", 8))));
  }

  @Test
  public void dquote() throws IOException {

    loadBasePackage();

    assertThat( eval(" dQuote('a') "), equalTo( c("\"a\"")) );
  }


  @Test
  public void formals() throws IOException {

    loadBasePackage();

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

    eval("f<-function(a,b) a+b ");
    eval("x<-c(1)");
    assertThat( eval("lapply(x,f,2) "), equalTo(list(3d)));
  }

  @Test 
  public void genericSubscript() throws IOException {
    assumingBasePackagesLoad();

    eval("  d<-as.data.frame(list(ids=1:5)) ");
    assertThat( eval(" d[,1] "), equalTo( c_i(1,2,3,4,5)));

  }

  @Test 
  public void factor() throws IOException {
    assumingBasePackagesLoad();
    
    eval(" cat <- factor(c(1:3), exclude= c(NA, NaN)) ");
    eval(" addNA(cat, ifany=TRUE) ");
    assertThat( eval("levels(cat)"), equalTo(c("1", "2", "3")));
    
    eval("nl <- length(ll <- levels(cat))");
    
    assertThat( eval("nl"), equalTo(c_i(3)));
  
    eval("exclude <- NA");
    eval("exclude <- as.vector(exclude, typeof(c(1,2,NA)))");
    assertThat(eval("is.na(exclude)"), equalTo(c(true)));
    
    // ensure that NA is NOT added as a level
    eval(" cat <- factor(c(1,2,NA)) ");
    assertThat( eval("levels(cat)"), equalTo(c("1", "2")));
    
  
  }
  

  @Test
  public void factorInteger() throws IOException {
    assumingBasePackagesLoad();
    
    eval("x <- 1:5");
    eval("exclude <- c(NA, NaN)");
    
    eval("y <- unique(x)");
    
    assertThat( eval("y"), equalTo(c_i(1,2,3,4,5)));
    
    eval("ind <- sort.list(y)");
    eval("y <- as.character(y)");
    eval("levels <- unique(y[ind])");
    
    assertThat( eval("levels"), equalTo(c("1","2","3","4", "5")));
    
    eval("force(ordered)");
    eval("exclude <- as.vector(exclude, typeof(x))");
    
    assertThat( eval("exclude"), equalTo( c_i(IntVector.NA, IntVector.NA)));
    
    eval("x <- as.character(x)");
    eval("levels <- levels[is.na(match(levels, exclude))]");
    
    assertThat( eval("levels"), equalTo(c("1","2","3","4","5")));
  }
  
  @Test
  public void setRowNames() throws IOException {
    assumingBasePackagesLoad();
    
    eval(" xi <- list(c(1:5))");
    eval(" class(xi) <- 'data.frame'");
    eval(" attr(xi, 'row.names') <- c(NA,-5)");
   
    assertThat( eval(" .row_names_info(xi) "), equalTo( c_i(-5)));
  }

  @Test
  public void factorIssue10() throws IOException {
    assumingBasePackagesLoad();
    
    eval(" gender <- c('F','F','F','F', 'M','M','M') ");
    eval(" gender <- factor(gender) ");
    
    assertThat( eval("class(gender) "), equalTo(c("factor")));
  }
  
  @Test
  public void factorPrint() throws IOException {
    assumingBasePackagesLoad();
    
    StringWriter stringWriter = new StringWriter();
    topLevelContext.getGlobals().setStdOut(new PrintWriter(stringWriter));
    
    eval(" gender <- factor(c('F','F','F','F', 'M','M','M'))");
    eval(" print(gender) ");
    
    assertThat(stringWriter.toString(), equalTo("[1] F F F F M M M\nLevels: F M\n"));
  }
  
  @Test
  public void parentFrameFromWithinEval() throws IOException {
    assumingBasePackagesLoad();
    
    eval("qq<-99");
    eval("g<-function(envir=parent.frame()) envir ");
    eval("env<-eval(parse(text= 'qq<-101;g() '), envir=parent.frame())");
    
    assertThat(eval("env$qq"), equalTo(c(101)));
  }
  

  @Test
  public void parse() throws IOException {
    loadBasePackage();

    assertThat(eval(" parse(text='1') "), equalTo(expression(1d)));

  }

  @Test
  public void sapply() throws IOException {
    assumingBasePackagesLoad();
    
    eval(" x<-list() ");
    assertThat(eval("sapply(attr(~1,'vars'), deparse, width.cutoff = 500)[-1L]"), equalTo(list()));
  }

  @Test
  public void fork() throws IOException {
    assumingBasePackagesLoad();

    Context context1 = topLevelContext.fork();
    context1.evaluate( FunctionCall.newCall(Symbol.get("search")));

    Context context2 = topLevelContext.fork();

  }

  @Test @Ignore("not working yet")
  public void lzmaDecompression() throws IOException {
    assumingBasePackagesLoad();
    
    eval("data(USArrests)");
    eval("names(USArrests)");
  }
  
  @Test
  public void asDataFrameForMatrix() throws IOException {
    assumingBasePackagesLoad();
    
    eval("g<-matrix(1:64,8)");
    eval("df<-as.data.frame(g)");
    assertThat(eval("length(unclass(df))"), equalTo(c_i(8)));
  }
  
  @Test
  public void factorEquality() throws IOException {
    assumingBasePackagesLoad();

    eval("y <- as.factor(c(1,0))");
    assertThat( eval("y == c('1', '0')"), equalTo(c(true,true)));
  }
  
  @Test
  public void outer() throws IOException {
    assumingBasePackagesLoad();
    
    eval("x <- c(1,0,1,0,1,0)");
    eval("y <- as.factor(c(1,0,1,0,1,0))");
    eval("h <- levels(y)");
    
    assertThat( eval("Y <- rep(h, rep.int(length(y), length(h)))"), equalTo(c("0","0","0","0","0","0","1","1","1","1","1","1")));
    
    eval("X <- rep(y, times = ceiling(length(h)/length(y)))");
    assertThat(eval("class(X)"), equalTo(c("factor")));
    
    eval("yp <- ifelse(outer(y,h,'=='),1,0)");
    assertThat(eval("dim(yp)"), equalTo(c_i(6,2)));
    assertThat(eval("c(yp)"), equalTo(c(0,1,0,1,0,1,1,0,1,0,1,0)));
  }
  
  @Test
  public void issue8() throws IOException {
    assumingBasePackagesLoad();
    
    assertThat( eval("rep(seq(1,10,1),2)"), equalTo(c( 1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10)));
  }

  @Test
  public void source() throws IOException {
    assumingBasePackagesLoad();
    
    global.setVariable(Symbol.get("fn"), 
        new StringVector(BasePackageTest.class.getResource("SourceTest.R").getFile()));
      eval("source(fn)");
  }
  
  @Test
  public void splitAssign() throws IOException {
    assumingBasePackagesLoad();
    
    eval("n <- 10");
    eval("nn <- 100");
    eval("g <- factor(round(n * runif(n * nn)))");
    eval("x <- rnorm(n * nn) + sqrt(as.double(g))");
    eval("xg <- split(x, g)");
    eval("zz <- x");
    eval("lresult <- lapply(split(x, g), scale)");
    eval("split(zz, g) <- lresult");
  }
  
  @Test
  public void remove() throws IOException {
    assumingBasePackagesLoad();
    
    eval("a<-1");
    eval("remove(a)");
  }

  private void loadBasePackage() throws IOException {
    topLevelContext.init();
  }

  @Test
  public void bquote() throws IOException {
    assumingBasePackagesLoad();
    
    eval("x <- bquote(~0 + .(quote(births)))");
    eval("print(x)");

    // expected : ~0 + births 
    
    FunctionCall tildeCall = (FunctionCall) topLevelContext.getGlobalEnvironment().getVariable("x");
    assertThat(tildeCall.getFunction(), equalTo((SEXP)symbol("~")));    
    assertThat(tildeCall.getArguments().length(), equalTo(1));
    
    FunctionCall plusCall = (FunctionCall)tildeCall.getArgument(0);
    assertThat(plusCall.getFunction(), equalTo((SEXP)symbol("+")));    
  }
  
  @Test
  public void rowSums() throws IOException {
    assumingBasePackagesLoad();
    
    eval("m <- matrix(1:12, 3)");
    
    assertThat(eval("rowsum(m, group=c(1,1,1))"), equalTo(c_i(6,15,24,33)));
    assertThat(eval("row.names(rowsum(m, group=c(1,1,1)))"), equalTo(c("1")));

    assertThat(eval("rowsum(m, group=c(3,3,1), reorder=TRUE)"), equalTo(c_i(3,3,6,9,9,15,12,21)));

  }
  
  @Test
  public void rowLabelsFromFactors() throws IOException {
    assumingBasePackagesLoad();
    
    eval("x <- factor(c('Yes','No','No'))");
    eval("m <- matrix(c(1:6), 3, 2)");
    eval("rownames(m) <- unique(x)");
    assertThat(eval("rownames(m)"), equalTo(c("Yes","No")));
  }
  
  @Test
  @Ignore("todo")
  public void kendallCor() throws IOException {
    
    
    
  }
  
  @Test
  public void inOpWithNA() throws IOException {
    assumingBasePackagesLoad();
    
    assertThat( eval("NA %in% FALSE"), equalTo(c(false)));
    assertThat( eval("NA %in% TRUE"), equalTo(c(false))); 
  }
  
  @Test
  public void summaryForDataFrame() throws IOException {
    assumingBasePackagesLoad();
    eval(" x <-as.data.frame(list(x=1:10,y=11:20)) ");
    
    assertThat(eval("max(x)"), equalTo(c_i(20)));
  }
 
  @Test
  public void emptyFactor() {
    assumingBasePackagesLoad();
    
    eval("x <- factor() ");
    assertThat(eval("class(x)"), equalTo(c("factor")));
    assertThat(eval("attr(x,'levels')"), equalTo((SEXP)StringVector.EMPTY));
    assertThat(eval("typeof(x)"), equalTo(c("integer")));
    assertThat(eval("is.factor(x)"), equalTo(c(true)));
  }
 
}
