/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.base;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Tests that ensure the primitives integrate nicely with the 
 * R-language functions of the base package
 */
public class BasePackageTest extends EvalTestCase {


  @Test
  public void loadBase() throws IOException {

    topLevelContext.init();

    StringVector letters = (StringVector) eval("letters");
    assertThat( letters.getElementAsString(0),  equalTo( "a" ));
    assertThat( letters.getElementAsString(25), equalTo( "z" ));

    eval( "assign('x', 42) ");
    assertThat( eval( "x" ) , elementsIdenticalTo( c(42) ));

    // make sure that closures are enclosed by the base namspace
    Closure closure = (Closure)getValue( topLevelContext.getSession().getBaseEnvironment(), "backsolve" );
    assertThat( closure.getEnclosingEnvironment(), identicalTo(topLevelContext.getSession().getBaseNamespaceEnv() ));


    // make sure that base scripts are populated in both the base environment and the base namespace
    assertThat( getValue( topLevelContext.getSession().getBaseEnvironment(), "letters" ).length(), equalTo( 26 ));
    assertThat( getValue( topLevelContext.getSession().getBaseNamespaceEnv(), "letters" ).length(), equalTo( 26 ));

  }

  private SEXP getValue(Environment env, String name) {
    SEXP value = env.getVariable(topLevelContext, name);
    if(value instanceof Promise) {
      value = value.force(topLevelContext);
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

    assertThat(eval(" x >= y"), elementsIdenticalTo(c(true)));
  }

  @Test
  public void startsWith() throws IOException {
    loadBasePackage();

    eval(" x1 <- c('hel', 'lo w', 'orld', NA, NULL) ");
    eval(" x2 <- logical(5) ");
    eval(" x3 <- character(0) ");
    eval(" v1 <- c('h', 'w') ");
    eval(" v2 <- c('h', NA) ");
    eval(" v3 <- c('h', NULL) ");
    eval(" v4 <- NULL ");
    eval(" v5 <- character(0) ");
    eval(" v6 <- logical(0) ");
    eval(" v7 <- numeric(0) ");

    assertThat(eval(" startsWith(x1, v1) "), elementsIdenticalTo(c(Logical.TRUE, Logical.FALSE, Logical.FALSE, Logical.NA)));
    assertThat(eval(" startsWith(x1, v2) "), elementsIdenticalTo(c(Logical.TRUE, Logical.NA, Logical.FALSE, Logical.NA)));
    assertThat(eval(" startsWith(x1, v3) "), elementsIdenticalTo(c(Logical.TRUE, Logical.FALSE, Logical.FALSE, Logical.NA)));
    assertThat(eval(" startsWith(x1, v5) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" startsWith(x3, v1) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" startsWith(x3, v2) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" startsWith(x3, v3) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" startsWith(x3, v5) "), elementsIdenticalTo(LogicalVector.EMPTY));
  }

  @Test
  public void endsWith() throws IOException {
    loadBasePackage();

    eval(" x1 <- c('hel', 'lo w', 'orld', NA, NULL) ");
    eval(" x2 <- logical(5) ");
    eval(" x3 <- character(0) ");
    eval(" v1 <- c('h', 'w') ");
    eval(" v2 <- c('h', NA) ");
    eval(" v3 <- c('h', NULL) ");
    eval(" v4 <- NULL ");
    eval(" v5 <- character(0) ");
    eval(" v6 <- logical(0) ");
    eval(" v7 <- numeric(0) ");

    assertThat(eval(" endsWith(x1, v1) "), elementsIdenticalTo(c(Logical.FALSE, Logical.TRUE, Logical.FALSE, Logical.NA)));
    assertThat(eval(" endsWith(x1, v2) "), elementsIdenticalTo(c(Logical.FALSE, Logical.NA, Logical.FALSE, Logical.NA)));
    assertThat(eval(" endsWith(x1, v3) "), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE, Logical.FALSE, Logical.NA)));
    assertThat(eval(" endsWith(x1, v5) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" endsWith(x3, v1) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" endsWith(x3, v2) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" endsWith(x3, v3) "), elementsIdenticalTo(LogicalVector.EMPTY));
    assertThat(eval(" endsWith(x3, v5) "), elementsIdenticalTo(LogicalVector.EMPTY));
  }


  @Test
  public void versionCompare2() throws IOException {
    loadBasePackage();

    eval(" x <- as.numeric_version('2.10.1') ");
    eval(" y <- as.numeric_version('2.2.0') ");

    assertThat(eval(" x >= y"), elementsIdenticalTo(c(true)));
  }

  @Test
  public void oldRowNamesAreConverted() throws IOException {
    

    eval(" xi <- list(c(55, 60, 30, 40, 11)) ");
    eval(" attr(xi, 'row.names') <- c(NA, -5) ");
    eval(" class(xi) <- 'data.frame' ");

    assertThat( eval(" identical(attr(xi, 'row.names'),  c('1','2','3','4','5') ) "), elementsIdenticalTo(c(true)));
    assertThat( eval(" identical(attributes(xi)$row.names, c('1','2','3','4','5'))"), elementsIdenticalTo(c(true)));
    assertThat( eval(" identical(row.names(xi), c('1','2','3','4','5')) "), elementsIdenticalTo(c(true)) );
  }

  @Test
  public void fileInfo() throws IOException {

    loadBasePackage();

    eval("info <- file.info('" + getClass().getResource("/org/renjin/sexp/SEXP.class").getFile() + "')");

    assertThat(eval("info$isdir"), elementsIdenticalTo(c(false)));
    assertThat(eval("info$mode"), elementsIdenticalTo(c_i(Integer.parseInt("666", 8))));
  }

  @Test
  public void dquote() throws IOException {

    loadBasePackage();

    assertThat( eval(" dQuote('a') "), elementsIdenticalTo( c("\"a\"")) );
  }


  @Test
  public void formals() throws IOException {

    loadBasePackage();

    eval("g <- function() sys.parent() ");
    eval("f <- function() g() ");

    assertThat( eval("f()"), elementsIdenticalTo(c_i(1)));

    eval("g<-function() eval(formals(sys.function(sys.parent()))[['event']]) ");
    eval("f<-function(event=c('a','b','c')) g() ");

    SEXP result = eval("f(1) ");
    assertThat(result, identicalTo(c("a", "b", "c")));
  }

  @Test
  public void sysFunction() {
    

    eval("g <- function() { y <- 99; x<- 42; function() { sys.function() }  };");
    eval("fn <- g()");
    assertThat(eval("environment(fn)$x"), elementsIdenticalTo(c(42)));
  }

  @Test
  public void lapply() throws Exception {
    loadBasePackage();

    eval("f<-function(a,b) a+b ");
    eval("x<-c(1)");
    assertThat( eval("lapply(x,f,2) "), elementsIdenticalTo(list(3d)));
  }

  @Test
  public void genericSubscript() throws IOException {
    

    eval("  d<-as.data.frame(list(ids=1:5)) ");
    assertThat( eval(" d[,1] "), elementsIdenticalTo(c_i(1,2,3,4,5)));

  }

  @Test
  public void factor() throws IOException {
    

    eval(" cat <- factor(c(1:3), exclude= c(NA, NaN)) ");
    eval(" addNA(cat, ifany=TRUE) ");
    assertThat( eval("levels(cat)"), elementsIdenticalTo(c("1", "2", "3")));

    eval("nl <- length(ll <- levels(cat))");

    assertThat( eval("nl"), elementsIdenticalTo(c_i(3)));

    eval("exclude <- NA");
    eval("exclude <- as.vector(exclude, typeof(c(1,2,NA)))");
    assertThat(eval("is.na(exclude)"), elementsIdenticalTo(c(true)));

    // ensure that NA is NOT added as a level
    eval(" cat <- factor(c(1,2,NA)) ");
    assertThat( eval("levels(cat)"), elementsIdenticalTo(c("1", "2")));


  }


  @Test
  public void factorInteger() throws IOException {
    

    eval("x <- 1:5");
    eval("exclude <- c(NA, NaN)");

    eval("y <- unique(x)");

    assertThat( eval("y"), elementsIdenticalTo(c_i(1,2,3,4,5)));

    eval("ind <- sort.list(y)");
    eval("y <- as.character(y)");
    eval("levels <- unique(y[ind])");

    assertThat( eval("levels"), elementsIdenticalTo(c("1","2","3","4", "5")));

    eval("force(ordered)");
    eval("exclude <- as.vector(exclude, typeof(x))");

    assertThat( eval("exclude"), elementsIdenticalTo( c_i(IntVector.NA, IntVector.NA)));

    eval("x <- as.character(x)");
    eval("levels <- levels[is.na(match(levels, exclude))]");

    assertThat( eval("levels"), elementsIdenticalTo(c("1","2","3","4","5")));
  }

  @Test
  public void factorIssue10() throws IOException {
    

    eval(" gender <- c('F','F','F','F', 'M','M','M') ");
    eval(" gender <- factor(gender) ");

    assertThat( eval("class(gender) "), elementsIdenticalTo(c("factor")));
  }

  @Test
  public void factorPrint() throws IOException {
    

    StringWriter stringWriter = new StringWriter();
    topLevelContext.getSession().setStdOut(new PrintWriter(stringWriter));

    eval(" gender <- factor(c('F','F','F','F', 'M','M','M'))");
    eval(" print(gender) ");

    assertThat(stringWriter.toString().replace("\r\n", "\n"), equalTo("[1] F F F F M M M\nLevels: F M\n"));
  }

  @Test
  public void parentFrameFromWithinEval() throws IOException {
    

    eval("qq<-99");
    eval("g<-function(envir=parent.frame()) envir ");
    eval("env<-eval(parse(text= 'qq<-101;g() '), envir=parent.frame())");

    assertThat(eval("env$qq"), elementsIdenticalTo(c(101)));
  }



  @Test
  public void parse() throws IOException {
    loadBasePackage();

    SEXP mtime = eval("mtime <- \"2018-10-08 09:50:54 CEST\"");
    SEXP cdir = eval("getwd()");
    eval("text <- \"x <- 1 + 1\n" +
        "    y <- 2 + 2\"");
    eval("filename <- \"<text>\"");
    eval("srcfile <- srcfilecopy(filename, text, mtime)");
    eval("parsed <- parse(text = text, keep.source = TRUE, srcfile = srcfile)");

    assertThat(
        eval("attr(parsed, \"srcfile\")[[\"timestamp\"]]"),
        identicalTo(mtime)
    );

    SEXP sexp = eval(" parse(text='1', keep.source=TRUE) ");

    SEXP srcref = sexp.getAttribute(Symbols.SRC_REF).getElementAsSEXP(0);
    assertThat(srcref.getS3Class(), elementsIdenticalTo(c("srcref")));

    SEXP srcfile = srcref.getAttribute(Symbols.SRC_FILE);
    assertThat(srcfile, instanceOf(Environment.class));
    assertTrue(srcfile.inherits("srcfilecopy"));
    assertTrue(srcfile.inherits("srcfile"));
  }

  @Test
  public void sapply() throws IOException {
    

    eval(" x<-list() ");
    assertThat(eval("sapply(attr(~1,'vars'), deparse, width.cutoff = 500)[-1L]"), elementsIdenticalTo(list()));
  }

  @Test @Ignore("not working yet")
  public void lzmaDecompression() throws IOException {
    eval("data(USArrests)");
    eval("names(USArrests)");
  }

  @Test
  public void asDataFrameForMatrix() throws IOException {
    eval("g<-matrix(1:64,8)");
    eval("df<-as.data.frame(g)");
    assertThat(eval("length(unclass(df))"), elementsIdenticalTo(c_i(8)));
  }

  @Test
  public void factorEquality() throws IOException {
    eval("y <- as.factor(c(1,0))");
    assertThat( eval("y == c('1', '0')"), elementsIdenticalTo(c(true,true)));
  }

  @Test
  public void outer() throws IOException {
    eval("x <- c(1,0,1,0,1,0)");
    eval("y <- as.factor(c(1,0,1,0,1,0))");
    eval("h <- levels(y)");

    assertThat( eval("Y <- rep(h, rep.int(length(y), length(h)))"), elementsIdenticalTo(c("0","0","0","0","0","0","1","1","1","1","1","1")));

    eval("X <- rep(y, times = ceiling(length(h)/length(y)))");
    assertThat(eval("class(X)"), elementsIdenticalTo(c("factor")));

    eval("yp <- ifelse(outer(y,h,'=='),1,0)");
    assertThat(eval("dim(yp)"), elementsIdenticalTo(c_i(6,2)));
    assertThat(eval("c(yp)"), elementsIdenticalTo(c(0,1,0,1,0,1,1,0,1,0,1,0)));
  }

  @Test
  public void issue8() throws IOException {
    assertThat( eval("rep(seq(1,10,1),2)"), elementsIdenticalTo(c( 1,2,3,4,5,6,7,8,9,10,1,2,3,4,5,6,7,8,9,10)));
  }

  @Test
  @Ignore("has dependency on utils package!")
  public void source() throws IOException {
    

    String file = BasePackageTest.class.getResource("SourceTest.R").getFile();
    global.setVariable(topLevelContext, Symbol.get("fn"),
        StringVector.valueOf(new File(file).getAbsolutePath()));
    eval("source(fn)");
  }

  @Test
  public void splitAssign() throws IOException {
    

    eval("n <- 10");
    eval("nn <- 100");
    eval("g <- factor(2+round(sin(1:(n*nn)*(pi/6))))");
    eval("x <- rep(c(6,4,3,1,9), length.out=n * nn) + sqrt(as.double(g))");
    eval("xg <- split(x, g)");
    eval("zz <- x");
    eval("lresult <- lapply(split(x, g), scale)");
    eval("split(zz, g) <- lresult");
  }

  @Test
  public void remove() throws IOException {
    

    eval("a<-1");
    eval("remove(a)");
  }

  private void loadBasePackage() throws IOException {
    topLevelContext.init();
  }

  @Test
  public void bquote() throws IOException {
    

    eval("x <- bquote(~0 + .(quote(births)))");
    eval("print(x)");

    // expected : ~0 + births 

    FunctionCall tildeCall = (FunctionCall) topLevelContext.getGlobalEnvironment().getVariable(topLevelContext, "x");
    assertThat(tildeCall.getFunction(), identicalTo(symbol("~")));
    assertThat(tildeCall.getArguments().length(), equalTo(1));

    FunctionCall plusCall = tildeCall.getArgument(0);
    assertThat(plusCall.getFunction(), identicalTo(symbol("+")));
  }

  @Test
  public void bquoteInternal() throws IOException {

    

    eval("tt <- 1");
    eval("bq <- bquote( ~ 0 + . (tt) )");

    assertThat(eval("bq[[1]]"), identicalTo(Symbol.get("~")));
    assertThat(eval("bq[[2]][[1]]"), identicalTo(Symbol.get("+")));
    assertThat(eval("bq[[2]][[2]]"), elementsIdenticalTo(c(0)));
    assertThat(eval("bq[[2]][[3]]"), elementsIdenticalTo(c(1)));
    //R outputs ~0 + 1, renjin 0 + 1 ~
    // expected : ~0 + births


  }


  @Test
  public void rowSums() throws IOException {
    

    eval("m <- matrix(1:12, 3)");

    assertThat(eval("rowsum(m, group=c(1,1,1))"), elementsIdenticalTo(c_i(6,15,24,33)));
    assertThat(eval("row.names(rowsum(m, group=c(1,1,1)))"), elementsIdenticalTo(c("1")));

    assertThat(eval("rowsum(m, group=c(3,3,1), reorder=TRUE)"), elementsIdenticalTo(c_i(3,3,6,9,9,15,12,21)));

  }

  @Test
  public void rowLabelsFromFactors() throws IOException {
    

    eval("x <- factor(c('Yes','No','No'))");
    eval("m <- matrix(c(1:6), 2, 3)");
    eval("rownames(m) <- unique(x)");
    assertThat(eval("rownames(m)"), elementsIdenticalTo(c("Yes","No")));
  }

  @Test
  @Ignore("todo")
  public void kendallCor() throws IOException {



  }

  @Test
  public void inOpWithNA() throws IOException {
    

    assertThat( eval("NA %in% FALSE"), elementsIdenticalTo(c(false)));
    assertThat( eval("NA %in% TRUE"), elementsIdenticalTo(c(false)));
  }

  @Test
  public void summaryForDataFrame() throws IOException {
    
    eval(" x <-as.data.frame(list(x=1:10,y=11:20)) ");

    assertThat(eval("max(x)"), elementsIdenticalTo(c_i(20)));
  }

  @Test
  public void emptyFactor() {
    

    eval("x <- factor() ");
    assertThat(eval("class(x)"), elementsIdenticalTo(c("factor")));
    assertThat(eval("attr(x,'levels')"), identicalTo(StringVector.EMPTY));
    assertThat(eval("typeof(x)"), elementsIdenticalTo(c("integer")));
    assertThat(eval("is.factor(x)"), elementsIdenticalTo(c(true)));
  }

  @Test
  public void attributeOverflow() {
    

    eval(" all.equal(list(names = NULL), list(names = NULL))");
  }

  @Test
  public void serialize() {

    eval("x <- serialize(42, connection=NULL)");
    assertThat(eval("length(x)"), elementsIdenticalTo(c_i(30)));
    assertThat(eval("x[1:6]"), identicalTo(raw(0x58, 0x0a, 0x00, 0x00, 0x00, 0x02)));
  }

  private SEXP raw(int... integers) {
    RawVector.Builder vector = new RawVector.Builder();
    for(int i : integers) {
      vector.add(i);
    }
    return vector.build();
  }

  @Test
  public void recall() {
    

    eval("fib <- function(n) if(n<=2) { if(n>=0) 1 else 0 } else Recall(n-1) + Recall(n-2)");
    eval("fibonacci <- fib");
    eval("rm(fib)");
    assertThat(eval("fibonacci(10)"), elementsIdenticalTo(c(55)));
  }

  @Test
  public void mapply() {
    

    assertThat(eval("mapply(rep, 1:4, 4:1)"), elementsIdenticalTo(list(
        c_i(1,1,1,1),
        c_i(2,2,2),
        c_i(3,3),
        c_i(4)
    )));
  }

  @Test
  public void assignInClosure() {
    

    eval(" f <- function() { y<-66; fieldClasses <- NULL; assign('fieldClasses', 42); fieldClasses; } ");

    assertThat(eval("f()"), elementsIdenticalTo(c(42)));

  }

  @Test
  public void ls() {
    
    eval("x<-41");
    eval(".Foo <- 'bar'");
    eval("print(ls(all.names=TRUE))");
  }

  @Test
  public void setBody() {
    
    eval("f <- function(x,y,z) y ");
    eval("body(f) <- quote(x) ");
    assertThat(eval("f(42)"), elementsIdenticalTo(c(42)));
  }

  @Test
  public void setFormals() {
    

    eval(" f <- function(x) {  .findNextFromTable(method, f, optional, envir) }");
    eval(" bd <- body(f)");
    eval(" print(typeof(if(is.null(bd) || is.list(bd)) list(bd) else bd)) ");
    eval(" value <-  alist(method=,f='<unknown>', mlist=,optional=FALSE,envir=) ");
    eval(" newf <- c(value, if(is.null(bd) || is.list(bd)) list(bd) else bd) ");
    eval(" print(newf) ");
    assertThat(eval("length(newf)"), elementsIdenticalTo(c_i(6)));
  }

  @Test
  public void isR() {
    
    assertThat(eval("is.R()"), elementsIdenticalTo(c(true)));
  }

  @Test
  public void cut() {

    assertThat(eval(" cut(c(1,2,3,4,5,6), breaks=c(0,2,6))"), elementsIdenticalTo(c_i(1,1,2,2,2,2)));
    assertThat(eval(" cut(c(1,2,3,4,5,6), breaks=c(0,2,6), right=F)"),
            elementsIdenticalTo(c_i(1,2,2,2,2,IntVector.NA)));
    assertThat(eval(" cut(c(1,2,3,4,5,6), breaks=c(0,2,6), right=F, include.lowest=T)"),
            elementsIdenticalTo(c_i(1,2,2,2,2,2)));
  }


}
