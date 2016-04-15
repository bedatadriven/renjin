package org.renjin;

import org.junit.Ignore;
import org.junit.Test;

/**
 * All failing microtests
 */
@Ignore
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
    public void micro2650() {
        assertIdentical("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1:3,2,2) ; f(1:3,\"X\",2) ; f(list(1,list(2)),c(2,1),4) }", "list(1, list(4))");
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
    public void micro2710() {
        assertIdentical("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- NULL ; l }", "list(1, list(2))");
    }
    @Test
    public void micro2711() {
        assertIdentical("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- 4 ; l }", "list(1, list(2, 4))");
    }
    @Test
    public void micro2714() {
        assertIdentical("{ l<-list(a=1,b=2,list(c=3,d=4,list(e=5:6,f=100))) ; l[[c(3,3,1)]] <- NULL ; l }", "structure(list(a = 1, b = 2, structure(list(c = 3, d = 4, structure(list(    f = 100), .Names = \"f\")), .Names = c(\"c\", \"d\", \"\"))), .Names = c(\"a\", \"b\", \"\"))");
    }
    @Test
    public void micro2715() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100 ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 3, 100), .Names = c(\"x\", \"y\", \"z\",     \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2716() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"z\")]] <- 100 ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 100), .Names = c(\"x\", \"y\", \"z\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2717() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\")]] <- NULL ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2), .Names = c(\"d\", \"e\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2719() {
        assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100L ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(c(1L, 2L, 3L, 100L), .Names = c(\"x\", \"y\", \"z\",     \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2720() {
        assertIdentical("{ l<-list(a=TRUE,b=FALSE,c=list(d=TRUE,e=FALSE,f=c(x=TRUE,y=FALSE,z=TRUE))) ; l[[c(\"c\",\"f\",\"zz\")]] <- TRUE ; l }", "structure(list(a = TRUE, b = FALSE, c = structure(list(d = TRUE,     e = FALSE, f = structure(c(TRUE, FALSE, TRUE, TRUE), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2721() {
        assertIdentical("{ l<-list(a=\"a\",b=\"b\",c=list(d=\"cd\",e=\"ce\",f=c(x=\"cfx\",y=\"cfy\",z=\"cfz\"))) ; l[[c(\"c\",\"f\",\"zz\")]] <- \"cfzz\" ; l }", "structure(list(a = \"a\", b = \"b\", c = structure(list(d = \"cd\",     e = \"ce\", f = structure(c(\"cfx\", \"cfy\", \"cfz\", \"cfzz\"), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2722() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- list(100) ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(list(x = 1, y = 2, z = 3, zz = list(100)), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2723() {
        assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- 100L ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = 100L), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2724() {
        assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- list(haha=\"gaga\") ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(list(haha = \"gaga\"), .Names = \"haha\")), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Test
    public void micro2727() {
        assertIdentical("{ l <- list(1, list(2)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2");
    }
    @Test
    public void micro2728() {
        assertIdentical("{ l <- list(1, list(2,3,4)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2");
    }
    @Test
    public void micro2734() {
        assertIdentical("{ x <- list(1,list(2,3),4) ; x[[c(2,3)]] <- 3 ; x }", "list(1, list(2, 3, 3), 4)");
    }
    @Test
    public void micro2735() {
        assertIdentical("{ x <- list(1,list(2,3),4) ; z <- x[[2]] ; x[[c(2,3)]] <- 3 ; z }", "list(2, 3)");
    }
    @Test
    public void micro2736() {
        assertIdentical("{ x <- list(1,list(2,3),4) ; z <- list(x,x) ; u <- list(z,z) ; u[[c(2,2,3)]] <- 6 ; unlist(u) }", "c(1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 6)");
    }
    @Test
    public void micro2737() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, 3) }", "list(1, 3, 3)");
    }
    @Test
    public void micro2738() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, NULL) }", "list(1, 3)");
    }
    @Test
    public void micro2739() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), \"hello\", 2) }", "structure(c(1, 2, 3, 2), .Names = c(\"\", \"\", \"\", \"hello\"))");
    }
    @Test
    public void micro2740() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(x=3)),c(\"b\",\"x\"),10) }", "structure(list(1, 2, b = structure(list(x = 10), .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
    }
    @Test
    public void micro2741() {
        assertIdentical("list(1,2,b=c(x=3))", "structure(list(1, 2, b = structure(3, .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=c(x=3)),c(\"b\",\"x\"),10) }", "structure(list(1, 2, b = structure(10, .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
    }
    @Test
    public void micro2743() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(a=list(x=1,y=2),3),4),c(\"b\",\"a\",\"x\"),10) }", "structure(list(1, 2, b = structure(list(a = structure(list(x = 10,     y = 2), .Names = c(\"x\", \"y\")), 3), .Names = c(\"a\", \"\")),     4), .Names = c(\"\", \"\", \"b\", \"\"))");
    }
    @Test
    public void micro2744() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=2),\"b\",NULL) }", "structure(list(a = 1), .Names = \"a\")");
    }
    @Test
    public void micro2745() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=list(2)),\"b\",double()) }", "structure(list(a = 1, b = numeric(0)), .Names = c(\"a\", \"b\"))");
    }
    @Test
    public void micro2746() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=c(a=2)),c(TRUE,TRUE),3) }", "structure(list(a = 3, b = structure(2, .Names = \"a\")), .Names = c(\"a\", \"b\"))");
    }
    @Test
    public void micro2747() {
        assertIdentical("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(2,2,3,2)]] <- 10 ; l }", "structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c(\"c\", \"d\"))), .Names = c(\"a\", \"b\", \"cd\"))");
    }
    @Test //@Ignore("Recursive setting")
    public void micro2748() {
        assertIdentical("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(\"xy\",\"y\",\"cd\",\"d\")]] <- 10 ; l }", "structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c(\"c\", \"d\"))), .Names = c(\"a\", \"b\", \"cd\"))");
    }
}
