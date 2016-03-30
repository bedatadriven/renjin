package org.renjin;

import org.junit.Ignore;
import org.junit.Test;

/**
 * All failing microtests
 */

public class FailingMicroTest extends AbstractMicroTest {


    @Test
    public void micro500() {
        assertIdentical("{  m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[[2,1,1]] <- v ; x } ; f(m,10L) ; f(m,10) ; x <- f(m,11L) ; x[1] == 1 && x[2] == 11 && x[3] == 3 }", "TRUE");
    }
    @Test
    public void micro561() {
        assertIdentical("{ x <- c(1L,3L,4L,NA) ; dim(x) <- c(2,1,2); x[[2,1,1]] <- list(10+1i); x[2] }", "list(list(10+1i))");
    }
    @Test
    public void micro572() {
        assertIdentical("{ x <- list(1,10,-1/0,0/0) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(list(TRUE)) ; z <- f(NA) ; unlist(z) }", "c(1, NA, -Inf, NaN)");
    }
    @Test
    public void micro578() {
        assertIdentical("{ for(i in 1:2) { if (i==1) { b <- as.list(11:14) } else { b <- c(1/0,-3/0,0/0,4) }; dim(b) <- c(2,1,2); b[[2,1,1]] <- list(111) } ; dim(b) <- NULL ; b }", "list(Inf, list(111), NaN, 4)");
    }
    @Test
    public void micro581() {
        assertIdentical("{ x <- 1:4 ; dim(x) <- c(1,1,4); x[,,NA] <- 12L ; as.integer(x) }", "1:4");
    }
    @Ignore @Test
    public void micro820() {
        assertIdentical("{ as.character(list(c(2L, 3L, 5L))) }", "\"c(2, 3, 5)\"");
    }
    @Test
    public void micro839() {
        assertIdentical("{ l <- 1 ; attr(l, 'my') <- 1; as.list(l) }", "list(1)");
    }
    @Test
    public void micro921() {
        assertIdentical("{ min(c(1,2,0/0)) }", "NaN");
    }
    @Test
    public void micro922() {
        assertIdentical("{ max(c(1,2,0/0)) }", "NaN");
    }
    @Test
    public void micro951() {
        assertIdentical("{ x <- 1:2 ; names(x) <- c('A',NA) ; c(x,test=x) }", "structure(c(1L, 2L, 1L, 2L), .Names = c(\"A\", NA, \"test.A\", \"test.NA\"))");
    }
    @Test
    public void micro954() {
        assertIdentical("{ c(1i,0/0) }", "c(0+1i, complex(real=NaN, i=0))");
    }
    @Test
    public void micro1101() {
        assertIdentical("{ cumsum(c(1,0/0,5+1i)) }", "c(1+0i, complex(real=NaN, i=0), complex(real=NaN, i=1))");
    }
    @Test
    public void micro1132() {
        assertIdentical("{ strsplit('helloh', 'h', fixed=TRUE) }", "list(c(\"\", \"ello\"))");
    }
    @Test
    public void micro1133() {
        assertIdentical("{ strsplit( c('helloh', 'hi'), c('h',''), fixed=TRUE) }", "list(c(\"\", \"ello\"), c(\"h\", \"i\"))");
    }
    @Test
    public void micro1137() {
        assertIdentical("{ strsplit('ahoj', split='') [[c(1,2)]] }", "\"h\"");
    }
    @Test
    public void micro1142() {
        assertIdentical("{ a <- as.raw(200) ; b <- as.raw(255) ; paste(a, b) }", "\"c8 ff\"");
    }
    @Test
    public void micro1149() {
        assertIdentical("{ substr('fastr', start=NA, stop=2) }", "NA_character_");
    }
    @Test
    public void micro1153() {
        assertIdentical("{ substring('fastr', first=NA, last=2) }", "NA_character_");
    }
    @Test
    public void micro1165() {
        assertIdentical("{ order(c(1,2,3,NA), na.last=FALSE) }", "c(4L, 1L, 2L, 3L)");
    }
    @Ignore @Test
    public void micro1169() {
        assertIdentical("{ order(c('a','z','Z','xxxz','zza','b')) }", "c(1L, 6L, 4L, 2L, 3L, 5L)");
    }
    @Ignore @Test
    public void micro1171() {
        assertIdentical("{ round( log(10,), digits = 5 ) }", "2.30259");
    }
    @Ignore @Test
    public void micro1177() {
        assertIdentical("{ round( exp(c(1+1i,-2-3i)), digits=5 ) }", "c(1.46869+2.28736i, -0.13398-0.0191i)");
    }
    @Ignore @Test
    public void micro1178() {
        assertIdentical("{ round( exp(1+2i), digits=5 ) }", "-1.1312+2.47173i");
    }
    @Ignore @Test
    public void micro1179() {
        assertIdentical("{ abs((-1-0i)/(0+0i)) }", "Inf");
    }
    @Ignore @Test
    public void micro1180() {
        assertIdentical("{ abs((-0-1i)/(0+0i)) }", "Inf");
    }
    @Test
    public void micro1182() {
        assertIdentical("{ abs(0/0) }", "NaN");
    }
    @Ignore @Test
    public void micro1186() {
        assertIdentical("{ exp(-abs((0+1i)/(0+0i))) }", "0");
    }
    @Ignore @Test
    public void micro1191() {
        assertIdentical("{ tolower(1E100) }", "\"1e+100\"");
    }
    @Ignore @Test
    public void micro1192() {
        assertIdentical("{ toupper(1E100) }", "\"1E+100\"");
    }
    @Ignore @Test
    public void micro1214() {
        assertIdentical("{ gregexpr('(a)[^a]\\\\1', c('andrea apart', 'amadeus', NA)) }", "list(structure(6L, match.length = 3L), structure(1L, match.length = 3L),     structure(NA_integer_, match.length = NA_integer_))");
    }
    @Test
    public void micro1226() {
        assertIdentical("{ x <- c(1,NA); names(x) <- c('hello','hi') ; cumsum(x) }", "structure(c(1, NA), .Names = c(\"hello\", \"hi\"))");
    }
    @Test
    public void micro1227() {
        assertIdentical("{ x <- c(1,NA); names(x) <- c(NA,'hi') ; cumsum(x) }", "structure(c(1, NA), .Names = c(NA, \"hi\"))");
    }
    @Test
    public void micro1230() {
        assertIdentical("{ x <- 1:2; names(x) <- c('A', 'B') ; abs(x) }", "structure(1:2, .Names = c(\"A\", \"B\"))");
    }
    @Test
    public void micro239() {
        assertIdentical("{ -(0/0) }", "NaN");
    }
    @Test
    public void micro1254() {
        assertIdentical("{ f <- function(z) { exists('z') } ; f(a) }", "TRUE");
    }
    @Test
    public void micro1376() {
        assertIdentical("{ m <- matrix(-5000:4999, nrow=100) ; sum(m * t(m)) }", "1666502500L");
    }
    @Test
    public void micro1377() {
        assertIdentical("{ m <- matrix(c(rep(1:10,100200),100L), nrow=1001) ; sum(m * t(m)) }", "38587000L");
    }
    @Test
    public void micro1393() {
        assertIdentical("{ is.numeric(TRUE) }", "FALSE");
    }
    @Test @Ignore("todo: attribute order?")
    public void micro1420() {
        assertIdentical("{ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attr(x, 'myatt1') <- 'hello' ; attributes(x) }", "structure(list(myatt = 2:4, myatt1 = \"hello\"), .Names = c(\"myatt\", \"myatt1\"))");
    }
    @Test
    public void micro1441() {
        assertIdentical("{ x <- list(a=list('1','2',b='3','4')) ; unlist(x) }", "structure(c(\"1\", \"2\", \"3\", \"4\"), .Names = c(\"a1\", \"a2\", \"a.b\", \"a4\"))");
    }
    @Test
    public void micro1442() {
        assertIdentical("{ x <- list(a=list('1','2',b=list('3'))) ; unlist(x) }", "structure(c(\"1\", \"2\", \"3\"), .Names = c(\"a1\", \"a2\", \"a.b\"))");
    }
    @Test
    public void micro1443() {
        assertIdentical("{ x <- list(a=list(1,FALSE,b=list(2:4))) ; unlist(x) }", "structure(c(1, 0, 2, 3, 4), .Names = c(\"a1\", \"a2\", \"a.b1\", \"a.b2\", \"a.b3\"))");
    }
    @Ignore("todo: recursive list indexing") 
    @Test
    public void micro1445() {
        assertIdentical("{ a = array(1:4,c(2,2)); b = aperm(a); (a[1,1] == b[1,1]) && (a[1,2] == b[2,1]) && (a[2,1] == b[1,2]) && (a[2,2] == b[2,2]); }", "TRUE");
    }
    @Ignore @Test
    public void micro1446() {
        assertIdentical("{ a = array(1:24,c(2,3,4)); b = aperm(a); dim(b)[1] == 4 && dim(b)[2] == 3 && dim(b)[3] == 2; }", "TRUE");
    }
    @Ignore @Test
    public void micro1447() {
        assertIdentical("{ a = array(1:24,c(2,3,4)); b = aperm(a, resize=FALSE); dim(b)[1] == 2 && dim(b)[2] == 3 && dim(b)[3] == 4; }", "TRUE");
    }
    @Ignore @Test
    public void micro1450() {
        assertIdentical("{ a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; }", "TRUE");
    }
    @Ignore @Test
    public void micro1451() {
        assertIdentical("{ a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,1,2]; }", "TRUE");
    }
    @Test
    @Ignore("todo: collation order")
    public void micro1490() {
        assertIdentical("{ sort(c('a','z','Z','xxxz','zza','b'), index.return=TRUE)$ix }", "c(1L, 6L, 4L, 2L, 3L, 5L)");
    }
    @Test
    @Ignore("todo: collation order")
    public void micro1495() {
        assertIdentical("{ sort(c('A','a'), decreasing=TRUE) }", "c(\"A\", \"a\")");
    }
    @Test
    @Ignore("todo: collation order")
    public void micro1496() {
        assertIdentical("{ sort(c('a','A'), decreasing=FALSE) }", "c(\"a\", \"A\")");
    }
    @Test
    @Ignore("") 
    public void micro1497() {
        assertIdentical("{ sort(c('a','A','z','Z','   01','01',NA), na.last=NA, decreasing=TRUE, index.return=TRUE)$ix }", "c(4L, 3L, 2L, 1L, 5L, 6L)");
    }
    @Ignore @Test
    public void micro1498() {
        assertIdentical("{ sort(c('a','A','z','Z','   01','01',NA), na.last=TRUE, decreasing=FALSE) }", "c(\"01\", \"   01\", \"a\", \"A\", \"z\", \"Z\", NA)");
    }
    @Ignore @Test
    public void micro1499() {
        assertIdentical("{ sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=FALSE, decreasing=FALSE) }", "c(NA, NA, NA, FALSE, TRUE, TRUE, TRUE)");
    }
    @Ignore @Test
    public void micro1500() {
        assertIdentical("{ sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=NA, decreasing=TRUE) }", "c(TRUE, TRUE, TRUE, FALSE)");
    }
    @Ignore @Test
    public void micro1507() {
        assertIdentical("{ rank(c(10,100,100,1000)) }", "c(1, 2.5, 2.5, 4)");
    }
    @Ignore @Test
    public void micro1508() {
        assertIdentical("{ rank(c(1000,100,100,100, 10)) }", "c(5, 3, 3, 3, 1)");
    }
    @Ignore @Test
    public void micro1509() {
        assertIdentical("{ rank(c(a=2,b=1,c=3,40)) }", "structure(c(2, 1, 3, 4), .Names = c(\"a\", \"b\", \"c\", \"\"))");
    }
    @Ignore @Test
    public void micro1510() {
        assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=NA) }", "structure(c(2, 1, 3, 4), .Names = c(\"a\", \"b\", \"c\", \"e\"))");
    }
    @Ignore @Test
    public void micro1511() {
        assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last='keep') }", "structure(c(2, 1, 3, NA, 4), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
    }
    @Ignore @Test
    public void micro1512() {
        assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=TRUE) }", "structure(c(2, 1, 3, 5, 4), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
    }
    @Ignore @Test
    public void micro1513() {
        assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=FALSE) }", "structure(c(3, 2, 4, 1, 5), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
    }
    @Ignore @Test
    public void micro1514() {
        assertIdentical("{ rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=FALSE, ties.method='max') }", "structure(c(3L, 3L, 5L, 1L, 5L), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
    }
    @Ignore @Test
    public void micro1515() {
        assertIdentical("{ rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=NA, ties.method='min') }", "structure(c(1L, 1L, 3L, 3L), .Names = c(\"a\", \"b\", \"c\", \"e\"))");
    }
    @Ignore @Test
    public void micro1521() {
        assertIdentical("{ round(det(matrix(c(1,2,4,5),nrow=2))) }", "-3");
    }
    @Ignore @Test
    public void micro1522() {
        assertIdentical("{ round(det(matrix(c(1,-3,4,-5),nrow=2))) }", "7");
    }
    @Ignore @Test
    public void micro1523() {
        assertIdentical("{ round(det(matrix(c(1,0,4,NA),nrow=2))) }", "NA_real_");
    }
    @Ignore @Test
    public void micro1525() {
        assertIdentical("{ fft(1:4, inverse=TRUE) }", "c(10+0i, -2-2i, -2+0i, -2+2i)");
    }
    @Ignore @Test
    public void micro1537() {
        assertIdentical("{ x <- qr(t(cbind(1:10,2:11)), LAPACK=TRUE) ; qr.coef(x, 1:2) }", "c(1, NA, NA, NA, NA, NA, NA, NA, NA, 0)");
    }
    @Ignore @Test
    public void micro1539() {
        assertIdentical("{ x <- qr(c(3,1,2), LAPACK=TRUE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) }", "0.71429");
    }
    @Ignore @Test
    public void micro1555() {
        assertIdentical("{ round(1.123456,digit=2.8) }", "1.123");
    }
    @Ignore @Test
    public void micro1569() {
        assertIdentical("{ f <- function(a = 2) { g(a) } ; g <- function(b) { missing(b) } ; f() }", "FALSE");
    }
    @Ignore @Test
    public void micro1570() {
        assertIdentical("{ f <- function(a = z) {  g(a) } ; g <- function(b) { missing(b) } ; f() }", "FALSE");
    }
    @Ignore @Test
    public void micro1641() {
        assertIdentical("{ sprintf('Hello %*2$d', 3, 2) }", "\"Hello  3\"");
    }
    @Test
    public void micro1643() {
        assertIdentical("{ sprintf('%4X', 26) }", "\"  1A\"");
    }
    
    @Test
    public void micro1644() {
        assertIdentical("{ sprintf('%04X', 26) }", "\"001A\"");
    }
    
    @Test
    public void micro1655() {
        assertIdentical("{ sprintf('Hello %d == %s', TRUE, TRUE) }", "\"Hello 1 == TRUE\"");
    }
    @Test
    public void micro1659() {
        assertIdentical("{ sprintf('Hello %f %f %f %f', 0/0, -1/0, 1/0, 1[2]) }", "\"Hello NaN -Inf Inf NA\"");
    }
    @Ignore @Test
    public void micro1660() {
        assertIdentical("{ sprintf('Hello %5.f %5.f %5.f %5.f', 0/0, -1/0, 1/0, 1[2]) }", "\"Hello   NaN  -Inf   Inf    NA\"");
    }
    @Ignore @Test
    public void micro1670() {
        assertIdentical("{ identical(0/0,1[2]) }", "FALSE");
    }
    @Test
    public void micro1704() {
        assertIdentical("{ identical(c(0/0,NA),c(NA,0/0)) }", "FALSE");
    }
    @Test
    public void micro1706() {
        assertIdentical("{ identical(c(1/0,-3/0),c(0/0,NA)) }", "FALSE");
    }
    @Ignore @Test
    public void micro1733() {
        assertIdentical("{ cur <- getwd(); cur1 <- setwd(c(cur, 'dummy')) ; cur2 <- getwd() ; cur == cur1 }", "TRUE");
    }
    @Ignore @Test
    public void micro1738() {
        assertIdentical("{ list.files('test/r/simple/data/tree1', pattern='*.tx') }", "character(0)");
    }
    @Ignore @Test
    public void micro1756() {
        assertIdentical("{ f <- function(a, b) { a + b } ; x <- 1 ; y <- 2 ; l <- call('f', x, y) ; x <- 10 ; eval(l) }", "3");
    }
    @Ignore @Test
    public void micro1957() {
        assertIdentical("{ cnt <- 1 ; delayedAssign(\"z\", evalat <<- cnt ) ; cnt <- 2 ; 'f<-' <- function(x, arg, value) { cnt <<- 4 ; arg * value } ; cnt <- 3; f(z, 12) <- 2 ; evalat }", "3");
    }
    @Ignore @Test
    public void micro1974() {
        assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(function(x) { 3+4i }, 10) }", "1");
    }
    @Ignore @Test
    public void micro2070() {
        assertIdentical("{ c(\"1L\",\"hello\") %in% 1:10 }", "c(FALSE, FALSE)");
    }
    @Ignore @Test
    public void micro2072() {
        assertIdentical("{ as.logical(-1:1) %in% TRUE }", "c(TRUE, FALSE, TRUE)");
    }
    @Test
    public void micro2171() {
        assertIdentical("{ x <- 1:3 ; x[[TRUE]] <- 10 ; x }", "c(10, 2, 3)");
    }
    @Test
    public void micro2173() {
        assertIdentical("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[\"hello\"] <- NULL ; b }", "list(1+2i, 3+4i)");
    }
    @Ignore @Test
    public void micro2238() {
        assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(1,2,3), character()) }", "numeric(0)");
    }
    @Ignore @Test
    public void micro2239() {
        assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(1,2,3), c(\"hello\",\"hi\")) }", "c(NA_real_, NA_real_)");
    }
    @Test
    public void micro2247() {
        assertIdentical("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-1)) }", "3");
    }
    @Test
    public void micro2248() {
        assertIdentical("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-2)) }", "2");
    }
    @Test
    public void micro2260() {
        assertIdentical("{ x<-c(); x[[TRUE]] <- 2; x }", "2");
    }
    
    @Ignore @Test
    public void micro2312() {
        assertIdentical("{ b <- as.raw(c(1,2)) ; b[as.double(NA)] <- as.raw(13) ; b }", "as.raw(c(0x01, 0x02))");
    }
    @Ignore @Test
    public void micro2323() {
        assertIdentical("{ x <- c(aa=TRUE) ; x[[\"a\"]] <- list(2L) ; x }", "structure(list(aa = TRUE, a = list(2L)), .Names = c(\"aa\", \"a\"))");
    }
    @Ignore @Test
    public void micro2327() {
        assertIdentical("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[3] <- NULL ; x }", "list(1, 2)");
    }
    @Test
    public void micro2331() {
        assertIdentical("{ x <- list(1,2) ; x[NA] <- NULL ; x }", "list(1, 2)");
    }
    
    @Ignore @Test
    public void micro2333() {
        assertIdentical("{ x <- list(1,2) ; x[-1] <- NULL ; x }", "list(1)");
    }
    
    @Test
    public void micro2337() {
        assertIdentical("{ x <- list(a=3,b=4) ; x[\"z\"] <- NULL ; x }", "structure(list(a = 3, b = 4), .Names = c(\"a\", \"b\"))");
    }
    @Ignore @Test
    public void micro2340() {
        assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,2,10) ; f(1:2,as.integer(NA), 10) }", "c(1, 2)");
    }
    @Ignore @Test
    public void micro2342() {
        assertIdentical("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[3] <- NULL ; b }", "list(1+2i, 3+4i)");
    }
    @Ignore @Test
    public void micro2381() {
        assertIdentical("{ x<-1:5 ; x[x[4]<-2] <- (x[4]<-100) ; x }", "c(1, 100, 3, 2, 5)");
    }
    @Ignore @Test
    public void micro2392() {
        assertIdentical("{ x <- c(a=1,b=2) ; x[2:3]<-10; x }", "structure(c(1, 10, 10), .Names = c(\"a\", \"b\", \"\"))");
    }
    @Ignore @Test
    public void micro2393() {
        assertIdentical("{ x <- c(a=1,b=2) ; x[c(2,3)]<-10; x }", "structure(c(1, 10, 10), .Names = c(\"a\", \"b\", \"\"))");
    }
    @Ignore @Test
    public void micro2394() {
        assertIdentical("{ x <- c(a=1,b=2) ; x[3]<-10; x }", "structure(c(1, 2, 10), .Names = c(\"a\", \"b\", \"\"))");
    }
    @Test
    public void micro2395() {
        assertIdentical("{ x <- matrix(1:2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "c(1, 2, 10)");
    }
    
    @Ignore @Test
    public void micro2397() {
        assertIdentical("{ x <- c(a=1,b=2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "structure(c(1, 2, 10), .Names = c(\"a\", \"b\", \"\"))");
    }
    @Test
    public void micro2399() {
        assertIdentical("{ x<-c(a=1,b=2,c=3) ; x[[\"d\"]]<-200; x }", "structure(c(1, 2, 3, 200), .Names = c(\"a\", \"b\", \"c\", \"d\"))");
    }
    @Test
    public void micro2404() {
        assertIdentical("{ a = c(1, 2); a[['a']] = 67; a; }", "structure(c(1, 2, 67), .Names = c(\"\", \"\", \"a\"))");
    }
    @Test
    public void micro2405() {
        assertIdentical("{ a = c(a=1,2,3); a[['x']] = 67; a; }", "structure(c(1, 2, 3, 67), .Names = c(\"a\", \"\", \"\", \"x\"))");
    }
    @Ignore @Test
    public void micro2474() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; m <- c(3,TRUE) ; dim(m) <- c(1,2) ; f(m, 1:2, l) }", "list(3, 5L)");
    }
    @Ignore @Test
    public void micro2491() {
        assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(3:4, c(1,2), c(10,11)) ; f(4:5, as.integer(NA), 2) }", "c(4, 5)");
    }
    @Ignore @Test
    public void micro2495() {
        assertIdentical("{ b <- c(1,2,5) ;  x <- as.double(NA) ; attr(x,\"my\") <- 2 ; b[c(1,NA,2)==2] <- x ; b }", "c(1, 2, NA)");
    }
    @Ignore @Test
    public void micro2499() {
        assertIdentical("{ b <- list(1,2,5) ; b[c(1,1,5)] <- NULL ; b }", "list(2, 5, NULL)");
    }
    @Ignore @Test
    public void micro2500() {
        assertIdentical("{ b <- list(1,2,5) ; b[c(-1,-4,-5,-1,-5)] <- NULL ; b }", "list(1)");
    }
    @Ignore @Test
    public void micro2501() {
        assertIdentical("{ b <- list(1,2,5) ; b[c(1,1,0,NA,5,5,7)] <- NULL ; b }", "list(2, 5, NULL, NULL)");
    }
    @Ignore @Test
    public void micro2502() {
        assertIdentical("{ b <- list(1,2,5) ; b[c(0,-1)] <- NULL ; b }", "list(1)");
    }
    @Ignore @Test
    public void micro2504() {
        assertIdentical("{ b <- list(x=1,y=2,z=5) ; b[c(0,-1)] <- NULL ; b }", "structure(list(x = 1), .Names = \"x\")");
    }
    @Ignore @Test
    public void micro2505() {
        assertIdentical("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,-1)] <- NULL ; b }", "list(1)");
    }
    @Ignore @Test
    public void micro2507() {
        assertIdentical("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(-10,-20,0)] <- NULL ; b }", "list()");
    }
    @Ignore @Test
    public void micro2509() {
        assertIdentical("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,3,5)] <- NULL ; b }", "list(1, 2, NULL)");
    }
    
    @Test
    public void micro2526() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,NA), 4) }", "c(3, 4, 5)");
    }
    @Test
    public void micro2527() {
        assertIdentical("{ b <- as.list(3:6) ; dim(b) <- c(4,1) ; b[c(TRUE,FALSE)] <- NULL ; b }", "list(4L, 6L)");
    }
    @Test
    public void micro2528() {
        assertIdentical("{ b <- as.list(3:6) ; names(b) <- c(\"X\",\"Y\",\"Z\",\"Q\") ; b[c(TRUE,FALSE)] <- NULL ; b }", "structure(list(Y = 4L, Q = 6L), .Names = c(\"Y\", \"Q\"))");
    }
    @Test
    public void micro2531() {
        assertIdentical("{ b <- as.list(3:6) ; dim(b) <- c(1,4) ; b[c(FALSE,FALSE,TRUE)] <- NULL ; b }", "list(3L, 4L, 6L)");
    }
    @Test
    public void micro2533() {
        assertIdentical("{ b <- as.list(3:5) ; dim(b) <- c(1,3) ; b[c(FALSE,TRUE,NA)] <- NULL ; b }", "list(3L, 5L)");
    }
    @Test
    public void micro2541() {
        assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), list(1+2i)) }", "list(1+2i, 2)");
    }
    @Test
    public void micro2542() {
        assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), 10) }", "list(10, 2)");
    }
   
    @Test
    public void micro2555() {
        assertIdentical("{ x <- c(1,0) ; z <- x ; x[c(NA,TRUE)] <- TRUE; x }", "c(1, 1)");
    }
    
    @Test
    public void micro2564() {
        assertIdentical("{ x <- 1:2 ; x[c(TRUE,NA)] <- 3L ; x }", "c(3L, 2L)");
    }
    
    @Test
    public void micro2566() {
        assertIdentical("{ x <- c(1L,2L) ; x[c(TRUE,NA)] <- 3L ; x }", "c(3L, 2L)");
    }
    
    @Test
    public void micro2577() {
        assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "c(FALSE, NA, FALSE, TRUE)");
    }
    @Test
    public void micro2578() {
        assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; z <- b ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "c(FALSE, NA, FALSE, TRUE)");
    }
    @Test
    public void micro2589() {
        assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,NA)] <- \"X\" ; b }", "c(\"X\", \"b\", \"c\")");
    }
    @Test
    public void micro2609() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(TRUE,TRUE,FALSE), NULL) }", "list(3)");
    }
    @Test
    public void micro2610() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, c(TRUE,TRUE,FALSE), NULL) ; z }", "list(3)");
    }
    @Test
    public void micro2644() {
        assertIdentical("{ l<-(list(list(1,2),c(3,4))); l[[c(2,1)]] }", "3");
    }
    @Test
    public void micro2648() {
        assertIdentical("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\",\"e\", \"f\")]] }", "4");
    }
    @Ignore @Test
    public void micro2650() {
        assertIdentical("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1:3,2,2) ; f(1:3,\"X\",2) ; f(list(1,list(2)),c(2,1),4) }", "list(1, list(4))");
    }
    @Ignore @Test
    public void micro2655() {
        assertIdentical("{ v<-1:3 ; v[[2]] <- list(100) ; v }", "list(1L, list(100), 3L)");
    }
    
    @Test
    public void micro2660() {
        assertIdentical("{ m<-list(1,2) ; m[TRUE] <- NULL ; m }", "list()");
    }
    @Test
    public void micro2661() {
        assertIdentical("{ m<-list(1,2) ; m[[TRUE]] <- NULL ; m }", "list(2)");
    }
    @Ignore @Test
    public void micro2665() {
        assertIdentical("{ l <- matrix(list(1,2)) ; l[3] <- NULL ; l }", "list(1, 2)");
    }
    @Ignore @Test
    public void micro2668() {
        assertIdentical("{ l <- matrix(list(1,2)) ; l[4] <- NULL ; l }", "list(1, 2, NULL)");
    }
    @Ignore @Test
    public void micro2671() {
        assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[5] <- NULL ; l}", "structure(list(a = 1, b = 2, c = 3, NULL), .Names = c(\"a\", \"b\", \"c\", \"\"))");
    }
    @Test
    public void micro2682() {
        assertIdentical("{ x <- 1:3 ; l <- list(1) ; l[[TRUE]] <- x ; l[[1]] }", "1:3");
    }
    @Ignore @Test
    public void micro2691() {
        assertIdentical("{ v<-list(1,2,3) ; v[c(2,3,NA,7,0)] <- NULL ; v }", "list(1, NULL, NULL, NULL)");
    }
    @Ignore @Test
    public void micro2693() {
        assertIdentical("{ v<-list(1,2,3) ; v[c(-1,-2,-6)] <- NULL ; v }", "list(1, 2)");
    }
    @Ignore @Test
    public void micro2694() {
        assertIdentical("{ v<-list(1,2,3) ; v[c(TRUE,FALSE,TRUE)] <- NULL ; v }", "list(2)");
    }
    @Ignore @Test
    public void micro2699() {
        assertIdentical("{ v<-list(1,2,3) ; v[c(TRUE,FALSE)] <- NULL ; v }", "list(2)");
    }
    @Ignore @Test
    public void micro2700() {
        assertIdentical("{ v<-list(1,2,3) ; v[c(TRUE,FALSE,FALSE,FALSE,FALSE,TRUE)] <- NULL ; v }", "list(2, 3, NULL, NULL)");
    }
    @Ignore @Test
    public void micro2701() {
        assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-3)] <- NULL ; l}", "structure(list(a = 1, c = 3), .Names = c(\"a\", \"c\"))");
    }
    @Ignore @Test
    public void micro2702() {
        assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-10)] <- NULL ; l}", "structure(list(a = 1), .Names = \"a\")");
    }
    @Ignore @Test
    public void micro2705() {
        assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,6)] <- NULL ; l}", "structure(list(a = 1, d = 4, NULL), .Names = c(\"a\", \"d\", \"\"))");
    }
    @Ignore @Test
    public void micro2706() {
        assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,TRUE,FALSE,TRUE)] <- NULL ; l}", "structure(list(c = 3), .Names = \"c\")");
    }
    @Ignore @Test
    public void micro2707() {
        assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE)] <- NULL ; l}", "structure(list(b = 2, d = 4), .Names = c(\"b\", \"d\"))");
    }
    @Ignore @Test
    public void micro2708() {
        assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE,FALSE,TRUE,FALSE,NA,TRUE,TRUE)] <- NULL ; l}", "structure(list(b = 2, c = 3, NULL, NULL), .Names = c(\"b\", \"c\", \"\", \"\"))");
    }
    @Ignore @Test
    public void micro2710() {
        assertIdentical("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- NULL ; l }", "list(1, list(2))");
    }
    @Ignore @Test
    public void micro2711() {
        assertIdentical("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- 4 ; l }", "list(1, list(2, 4))");
    }
    @Ignore @Test
    public void micro2714() {
        assertIdentical("{ l<-list(a=1,b=2,list(c=3,d=4,list(e=5:6,f=100))) ; l[[c(3,3,1)]] <- NULL ; l }", "structure(list(a = 1, b = 2, structure(list(c = 3, d = 4, structure(list(    f = 100), .Names = \"f\")), .Names = c(\"c\", \"d\", \"\"))), .Names = c(\"a\", \"b\", \"\"))");
    }
    @Ignore @Test
    public void micro2715() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100 ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 3, 100), .Names = c(\"x\", \"y\", \"z\",     \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2716() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"z\")]] <- 100 ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 100), .Names = c(\"x\", \"y\", \"z\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2717() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\")]] <- NULL ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2), .Names = c(\"d\", \"e\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2718() {
        assertIdentical("{ l<-list(a=1,b=2,c=3) ; l[c(\"a\",\"a\",\"a\",\"c\")] <- NULL ; l }", "structure(list(b = 2), .Names = \"b\")");
    }
    @Ignore @Test
    public void micro2719() {
        assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100L ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(c(1L, 2L, 3L, 100L), .Names = c(\"x\", \"y\", \"z\",     \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2720() {
        assertIdentical("{ l<-list(a=TRUE,b=FALSE,c=list(d=TRUE,e=FALSE,f=c(x=TRUE,y=FALSE,z=TRUE))) ; l[[c(\"c\",\"f\",\"zz\")]] <- TRUE ; l }", "structure(list(a = TRUE, b = FALSE, c = structure(list(d = TRUE,     e = FALSE, f = structure(c(TRUE, FALSE, TRUE, TRUE), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2721() {
        assertIdentical("{ l<-list(a=\"a\",b=\"b\",c=list(d=\"cd\",e=\"ce\",f=c(x=\"cfx\",y=\"cfy\",z=\"cfz\"))) ; l[[c(\"c\",\"f\",\"zz\")]] <- \"cfzz\" ; l }", "structure(list(a = \"a\", b = \"b\", c = structure(list(d = \"cd\",     e = \"ce\", f = structure(c(\"cfx\", \"cfy\", \"cfz\", \"cfzz\"), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2722() {
        assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- list(100) ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(list(x = 1, y = 2, z = 3, zz = list(100)), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2723() {
        assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- 100L ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = 100L), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2724() {
        assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- list(haha=\"gaga\") ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(list(haha = \"gaga\"), .Names = \"haha\")), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
    }
    @Ignore @Test
    public void micro2727() {
        assertIdentical("{ l <- list(1, list(2)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2");
    }
    @Ignore @Test
    public void micro2728() {
        assertIdentical("{ l <- list(1, list(2,3,4)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2");
    }
    @Ignore @Test
    public void micro2734() {
        assertIdentical("{ x <- list(1,list(2,3),4) ; x[[c(2,3)]] <- 3 ; x }", "list(1, list(2, 3, 3), 4)");
    }
    @Ignore @Test
    public void micro2735() {
        assertIdentical("{ x <- list(1,list(2,3),4) ; z <- x[[2]] ; x[[c(2,3)]] <- 3 ; z }", "list(2, 3)");
    }
    @Ignore @Test
    public void micro2736() {
        assertIdentical("{ x <- list(1,list(2,3),4) ; z <- list(x,x) ; u <- list(z,z) ; u[[c(2,2,3)]] <- 6 ; unlist(u) }", "c(1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 6)");
    }
    @Ignore @Test
    public void micro2737() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, 3) }", "list(1, 3, 3)");
    }
    @Ignore @Test
    public void micro2738() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, NULL) }", "list(1, 3)");
    }
    @Ignore @Test
    public void micro2739() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), \"hello\", 2) }", "structure(c(1, 2, 3, 2), .Names = c(\"\", \"\", \"\", \"hello\"))");
    }
    @Ignore @Test
    public void micro2740() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(x=3)),c(\"b\",\"x\"),10) }", "structure(list(1, 2, b = structure(list(x = 10), .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
    }
    @Ignore @Test
    public void micro2741() {
        assertIdentical("list(1,2,b=c(x=3))", "structure(list(1, 2, b = structure(3, .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=c(x=3)),c(\"b\",\"x\"),10) }", "structure(list(1, 2, b = structure(10, .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
    }
    @Ignore @Test
    public void micro2742() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(c(1,2,b=c(x=3)),c(\"b\"),10) }", "structure(c(1, 2, 3, 10), .Names = c(\"\", \"\", \"b.x\", \"b\"))");
    }
    @Ignore @Test
    public void micro2743() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(a=list(x=1,y=2),3),4),c(\"b\",\"a\",\"x\"),10) }", "structure(list(1, 2, b = structure(list(a = structure(list(x = 10,     y = 2), .Names = c(\"x\", \"y\")), 3), .Names = c(\"a\", \"\")),     4), .Names = c(\"\", \"\", \"b\", \"\"))");
    }
    @Ignore @Test
    public void micro2744() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=2),\"b\",NULL) }", "structure(list(a = 1), .Names = \"a\")");
    }
    @Ignore @Test
    public void micro2745() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=list(2)),\"b\",double()) }", "structure(list(a = 1, b = numeric(0)), .Names = c(\"a\", \"b\"))");
    }
    @Ignore @Test
    public void micro2746() {
        assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=c(a=2)),c(TRUE,TRUE),3) }", "structure(list(a = 3, b = structure(2, .Names = \"a\")), .Names = c(\"a\", \"b\"))");
    }
    @Ignore @Test
    public void micro2747() {
        assertIdentical("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(2,2,3,2)]] <- 10 ; l }", "structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c(\"c\", \"d\"))), .Names = c(\"a\", \"b\", \"cd\"))");
    }
    @Ignore @Test
    public void micro2748() {
        assertIdentical("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(\"xy\",\"y\",\"cd\",\"d\")]] <- 10 ; l }", "structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c(\"c\", \"d\"))), .Names = c(\"a\", \"b\", \"cd\"))");
    }
    @Ignore @Test
    public void micro2760() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),as.character(NA),as.complex(23)) }", "structure(c(13+0i, 14+0i, 23+0i), .Names = c(\"\", \"\", NA))");
    }
    @Ignore @Test
    public void micro2762() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c(\"\",\"\",\"\"),as.complex(23)) }", "structure(c(13+0i, 14+0i, 23+0i, 23+0i, 23+0i), .Names = c(\"\", \"\", \"\", \"\", \"\"))");
    }
    @Ignore @Test
    public void micro2763() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c(\"\",\"\",NA),as.complex(23)) }", "structure(c(13+0i, 14+0i, 23+0i, 23+0i, 23+0i), .Names = c(\"\", \"\", \"\", \"\", NA))");
    }
    @Ignore @Test
    public void micro2765() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"a\",\"X\",\"a\",\"b\"),list(3,TRUE,FALSE)) }", "structure(list(X = TRUE, a = FALSE, b = 3), .Names = c(\"X\", \"a\", \"b\"))");
    }
    @Ignore @Test
    public void micro2769() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),c(TRUE,NA)) }", "structure(c(1, 2, NA, 1), .Names = c(\"X\", \"a\", \"b\", NA))");
    }
    @Ignore @Test
    public void micro2771() {
        assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(list(X=1L,a=2L),c(\"X\",\"b\",NA),NULL) }", "structure(list(a = 2L), .Names = \"a\")");
    }
    @Test
    public void micro2772() {
        assertIdentical("{ b <- c(a=1+2i,b=3+4i) ; dim(b) <- c(2,1) ; b[c(\"a\",\"b\")] <- 3+1i ; b }", "structure(c(1+2i, 3+4i, 3+1i, 3+1i), .Names = c(\"\", \"\", \"a\", \"b\"))");
    }
    @Test
    public void micro2776() {
        assertIdentical("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[c(\"hello\",\"hi\")] <- NULL ; b }", "list(1+2i, 3+4i)");
    }


}
