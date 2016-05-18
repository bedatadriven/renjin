package org.renjin;

import org.junit.Ignore;
import org.junit.Test;

/**
 * All failing microtests
 */
//@Ignore
public class FailingMicroTest extends AbstractMicroTest {

  @Test
  public void micro820() {
    assertIdentical("{ as.character(list(c(2L, 3L, 5L))) }", "\"c(2, 3, 5)\"");
  }
  @Test
  public void micro1169() {
    assertIdentical("{ order(c('a','z','Z','xxxz','zza','b')) }", "c(1L, 6L, 4L, 2L, 3L, 5L)");
  }
  @Test
  public void micro1171() {
    assertIdentical("{ round( log(10,), digits = 5 ) }", "2.30259");
  }
  @Test
  public void micro1177() {
    assertIdentical("{ round( exp(c(1+1i,-2-3i)), digits=5 ) }", "c(1.46869+2.28736i, -0.13398-0.0191i)");
  }
  @Test
  public void micro1178() {
    assertIdentical("{ round( exp(1+2i), digits=5 ) }", "-1.1312+2.47173i");
  }
  @Test
  public void micro1179() {
    assertIdentical("{ abs((-1-0i)/(0+0i)) }", "Inf");
  }
  @Test
  public void micro1180() {
    assertIdentical("{ abs((-0-1i)/(0+0i)) }", "Inf");
  }
  @Test
  public void micro1186() {
    assertIdentical("{ exp(-abs((0+1i)/(0+0i))) }", "0");
  }
  @Test
  public void micro1191() {
    assertIdentical("{ tolower(1E100) }", "\"1e+100\"");
  }
  @Test
  public void micro1192() {
    assertIdentical("{ toupper(1E100) }", "\"1E+100\"");
  }
  @Test
  public void micro1214() {
    assertIdentical("{ gregexpr('(a)[^a]\\\\1', c('andrea apart', 'amadeus', NA)) }", "list(structure(6L, match.length = 3L), structure(1L, match.length = 3L),     structure(NA_integer_, match.length = NA_integer_))");
  }
  @Test //@Ignore("todo: attribute order?")
  public void micro1420() {
    assertIdentical("{ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attr(x, 'myatt1') <- 'hello' ; attributes(x) }", "structure(list(myatt = 2:4, myatt1 = \"hello\"), .Names = c(\"myatt\", \"myatt1\"))");
  }
  //@Ignore("todo: recursive list indexing")
  @Test
  public void micro1445() {
    assertIdentical("{ a = array(1:4,c(2,2)); b = aperm(a); (a[1,1] == b[1,1]) && (a[1,2] == b[2,1]) && (a[2,1] == b[1,2]) && (a[2,2] == b[2,2]); }", "TRUE");
  }
  @Test
  public void micro1446() {
    assertIdentical("{ " +
        "a = array(1:24,c(2,3,4)); " +
        "b = aperm(a); " +
        "dim(b)[1] == 4 && dim(b)[2] == 3 && dim(b)[3] == 2; }", "TRUE");
  }
  @Test
  public void micro1447() {
    assertIdentical("{ a = array(1:24,c(2,3,4)); b = aperm(a, resize=FALSE); dim(b)[1] == 2 && dim(b)[2] == 3 && dim(b)[3] == 4; }", "TRUE");
  }
  @Test
  public void micro1450() {
    assertIdentical("{ a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; }", "TRUE");
  }
  @Test
  public void micro1451() {
    assertIdentical("{ a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,1,2]; }", "TRUE");
  }
  @Test
  //@Ignore("todo: collation order")
  public void micro1490() {
    assertIdentical("{ sort(c('a','z','Z','xxxz','zza','b'), index.return=TRUE)$ix }", "c(1L, 6L, 4L, 2L, 3L, 5L)");
  }
  @Test
  //@Ignore("todo: collation order")
  public void micro1495() {
    assertIdentical("{ sort(c('A','a'), decreasing=TRUE) }", "c(\"A\", \"a\")");
  }
  @Test
  //@Ignore("todo: collation order")
  public void micro1496() {
    assertIdentical("{ sort(c('a','A'), decreasing=FALSE) }", "c(\"a\", \"A\")");
  }
  @Test
  public void micro1497() {
    assertIdentical("{ sort(c('a','A','z','Z','   01','01',NA), na.last=NA, decreasing=TRUE, index.return=TRUE)$ix }", "c(4L, 3L, 2L, 1L, 5L, 6L)");
  }
  @Test
  public void micro1498() {
    assertIdentical("{ sort(c('a','A','z','Z','   01','01',NA), na.last=TRUE, decreasing=FALSE) }", "c(\"01\", \"   01\", \"a\", \"A\", \"z\", \"Z\", NA)");
  }
  @Test
  public void micro1499() {
    assertIdentical("{ sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=FALSE, decreasing=FALSE) }", "c(NA, NA, NA, FALSE, TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro1500() {
    assertIdentical("{ sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=NA, decreasing=TRUE) }", "c(TRUE, TRUE, TRUE, FALSE)");
  }
  @Test
  public void micro1521() {
    assertIdentical("{ round(det(matrix(c(1,2,4,5),nrow=2))) }", "-3");
  }
  @Test
  public void micro1522() {
    assertIdentical("{ round(det(matrix(c(1,-3,4,-5),nrow=2))) }", "7");
  }
  @Test
  public void micro1523() {
    assertIdentical("{ round(det(matrix(c(1,0,4,NA),nrow=2))) }", "NA_real_");
  }
  @Test
  public void micro1525() {
    assertIdentical("{ fft(1:4, inverse=TRUE) }", "c(10+0i, -2-2i, -2+0i, -2+2i)");
  }
  @Test
  public void micro1537() {
    assertIdentical("{ x <- qr(t(cbind(1:10,2:11)), LAPACK=TRUE) ; qr.coef(x, 1:2) }", "c(1, NA, NA, NA, NA, NA, NA, NA, NA, 0)");
  }
  @Test
  public void micro1539() {
    assertIdentical("{ x <- qr(c(3,1,2), LAPACK=TRUE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) }", "0.71429");
  }
  @Test
  public void micro1555() {
    assertIdentical("{ round(1.123456,digit=2.8) }", "1.123");
  }
  @Test
  public void micro1641() {
    assertIdentical("{ sprintf('Hello %*2$d', 3, 2) }", "\"Hello  3\"");
  }
  @Test
  public void micro1660() {
    assertIdentical("{ sprintf('Hello %5.f %5.f %5.f %5.f', 0/0, -1/0, 1/0, 1[2]) }", "\"Hello   NaN  -Inf   Inf    NA\"");
  }
  @Test
  public void micro1670() {
    assertIdentical("{ identical(0/0,1[2]) }", "FALSE");
  }
  @Test
  public void micro1733() {
    assertIdentical("{ cur <- getwd(); cur1 <- setwd(c(cur, 'dummy')) ; cur2 <- getwd() ; cur == cur1 }", "TRUE");
  }
  @Test
  public void micro1738() {
    assertIdentical("{ list.files('test/r/simple/data/tree1', pattern='*.tx') }", "character(0)");
  }
  @Test
  public void micro1756() {
    assertIdentical("{ f <- function(a, b) { a + b } ; x <- 1 ; y <- 2 ; l <- call('f', x, y) ; x <- 10 ; eval(l) }", "3");
  }
  @Test
  public void micro1957() {
    assertIdentical("{ cnt <- 1 ; delayedAssign(\"z\", evalat <<- cnt ) ; cnt <- 2 ; 'f<-' <- function(x, arg, value) { cnt <<- 4 ; arg * value } ; cnt <- 3; f(z, 12) <- 2 ; evalat }", "3");
  }
  @Test
  public void micro1974() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(function(x) { 3+4i }, 10) }", "1");
  }
  @Test
  public void micro2070() {
    assertIdentical("{ c(\"1L\",\"hello\") %in% 1:10 }", "c(FALSE, FALSE)");
  }
  @Test
  public void micro2072() {
    assertIdentical("{ as.logical(-1:1) %in% TRUE }", "c(TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro2312() {
    assertIdentical("{ b <- as.raw(c(1,2)) ; b[as.double(NA)] <- as.raw(13) ; b }", "as.raw(c(0x01, 0x02))");
  }
  @Test
  public void micro2323() {
    assertIdentical("{ x <- c(aa=TRUE) ; x[[\"a\"]] <- list(2L) ; x }", "structure(list(aa = TRUE, a = list(2L)), .Names = c(\"aa\", \"a\"))");
  }
  @Test
  public void micro2327() {
    assertIdentical("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[3] <- NULL ; x }", "list(1, 2)");
  }
  @Test
  public void micro2333() {
    assertIdentical("{ x <- list(1,2) ; x[-1] <- NULL ; x }", "list(1)");
  }
  @Test
  public void micro2340() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,2,10) ; f(1:2,as.integer(NA), 10) }", "c(1, 2)");
  }
  @Test
  public void micro2342() {
    assertIdentical("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[3] <- NULL ; b }", "list(1+2i, 3+4i)");
  }
  @Test
  public void micro2381() {
    assertIdentical("{ x<-1:5 ; x[x[4]<-2] <- (x[4]<-100) ; x }", "c(1, 100, 3, 2, 5)");
  }
  @Test
  public void micro2397() {
    assertIdentical("{ x <- c(a=1,b=2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "structure(c(1, 2, 10), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro2474() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; m <- c(3,TRUE) ; dim(m) <- c(1,2) ; f(m, 1:2, l) }", "list(3, 5L)");
  }
  @Test
  public void micro2491() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(3:4, c(1,2), c(10,11)) ; f(4:5, as.integer(NA), 2) }", "c(4, 5)");
  }
  @Test
  public void micro2499() {
    assertIdentical("{ b <- list(1,2,5) ; b[c(1,1,5)] <- NULL ; b }", "list(2, 5, NULL)");
  }
  @Test
  public void micro2500() {
    assertIdentical("{ b <- list(1,2,5) ; b[c(-1,-4,-5,-1,-5)] <- NULL ; b }", "list(1)");
  }
  @Test
  public void micro2501() {
    assertIdentical("{ b <- list(1,2,5) ; b[c(1,1,0,NA,5,5,7)] <- NULL ; b }", "list(2, 5, NULL, NULL)");
  }
  @Test
  public void micro2502() {
    assertIdentical("{ b <- list(1,2,5) ; b[c(0,-1)] <- NULL ; b }", "list(1)");
  }
  @Test
  public void micro2504() {
    assertIdentical("{ b <- list(x=1,y=2,z=5) ; b[c(0,-1)] <- NULL ; b }", "structure(list(x = 1), .Names = \"x\")");
  }
  @Test
  public void micro2505() {
    assertIdentical("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,-1)] <- NULL ; b }", "list(1)");
  }
  @Test
  public void micro2507() {
    assertIdentical("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(-10,-20,0)] <- NULL ; b }", "list()");
  }
  @Test
  public void micro2509() {
    assertIdentical("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,3,5)] <- NULL ; b }", "list(1, 2, NULL)");
  }
  @Test
  public void micro2655() {
    assertIdentical("{ v<-1:3 ; v[[2]] <- list(100) ; v }", "list(1L, list(100), 3L)");
  }
  @Test
  public void micro2665() {
    assertIdentical("{ l <- matrix(list(1,2)) ; l[3] <- NULL ; l }", "list(1, 2)");
  }
  @Test
  public void micro2668() {
    assertIdentical("{ l <- matrix(list(1,2)) ; l[4] <- NULL ; l }", "list(1, 2, NULL)");
  }
  @Test
  public void micro2671() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[5] <- NULL ; l}", "structure(list(a = 1, b = 2, c = 3, NULL), .Names = c(\"a\", \"b\", \"c\", \"\"))");
  }
  @Test
  public void micro2691() {
    assertIdentical("{ v<-list(1,2,3) ; v[c(2,3,NA,7,0)] <- NULL ; v }", "list(1, NULL, NULL, NULL)");
  }
  @Test
  public void micro2693() {
    assertIdentical("{ v<-list(1,2,3) ; v[c(-1,-2,-6)] <- NULL ; v }", "list(1, 2)");
  }
  @Test
  public void micro2700() {
    assertIdentical("{ v<-list(1,2,3) ; v[c(TRUE,FALSE,FALSE,FALSE,FALSE,TRUE)] <- NULL ; v }", "list(2, 3, NULL, NULL)");
  }
  @Test
  public void micro2701() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-3)] <- NULL ; l}", "structure(list(a = 1, c = 3), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2702() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-10)] <- NULL ; l}", "structure(list(a = 1), .Names = \"a\")");
  }
  @Test
  public void micro2705() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,6)] <- NULL ; l}", "structure(list(a = 1, d = 4, NULL), .Names = c(\"a\", \"d\", \"\"))");
  }
  @Test
  public void micro2708() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE,FALSE,TRUE,FALSE,NA,TRUE,TRUE)] <- NULL ; l}", "structure(list(b = 2, c = 3, NULL, NULL), .Names = c(\"b\", \"c\", \"\", \"\"))");
  }
  @Test
  public void micro2722() {
    assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- list(100) ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(list(x = 1, y = 2, z = 3, zz = list(100)), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void microINDEX() {
    assertIdentical("{ x <- list(a=list(1:3)); x[[c(1,2,3)]] <- 4 }", "EvalException");
  }
  @Test
  public void microINDEX2() {
    assertIdentical("{ x <- list(1:3); x[[c(1,2,3)]] <- 4 }", "EvalException");
  }
  @Test
  public void microRESHAPE1() {
    assertIdentical("{ a <- reshape(Indometh, v.names = \"conc\", idvar = \"Subject\", timevar = \"time\", direction = \"wide\"); names(a)[5] }", "conc.1");
  }
  @Test
  public void microSTRSPLIT0() {
    assertIdentical("{ a <- strsplit(NA, \"d\"); a[[1]] }", "NULL");
  }
  @Test
  public void microSTRSPLIT1() {
    assertIdentical("{ a <- strsplit(\"abc\", \"d\"); a[[1]] }", "c(\"abc\")");
  }
  @Test
  public void microSTRSPLIT2() {
    assertIdentical("{ a <- strsplit(\"abc\", NA)[[1]]; a }", "c(\"abc\")");
  }
  @Test
  public void microGREPL1() {
    assertIdentical("{ a <- grepl(\"a\",c(\"a\",\"b\")); a }", "c(TRUE,FALSE)");
  }
  @Test
  public void microGREPL2() {
    assertIdentical("{ a <- grepl(NA,c(\"a\",\"b\")); a }", "c(NA_character_, NA_character_)");
  }
  @Test
  public void microSTRSPLIT3() {
    assertIdentical("{ a <- strsplit(\"abc\", NA); a[[1]] }", "c(\"abc\")");
  }
  @Test
  public void microGREP0() {
    assertIdentical("{ a <- grep(\"a\", c(\"a\",\"b\",NA,\"a\")); a }", "c(1L, 4L)");
  }
  @Test
  public void microGREP1() {
    assertIdentical("{ a <- grep(\"a\", NA); a }", "0L");
  }
  @Test
  public void microGREP2() {
    assertIdentical("{ a <- grep(NA, \"abc\"); a }", "NA_character_");
  }
  @Test
  public void microENC2UTF8_1() {
    assertIdentical("{ a <- enc2utf8(\"a\"); a }", "a");
  }
  @Test
  public void microENC2UTF8_2() {
    assertIdentical("{ a <- enc2utf8(c(\"a\",\"b\")); a }", "c(\"a\",\"b\")");
  }
}
