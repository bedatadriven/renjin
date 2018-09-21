#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#

library(hamcrest)

test.realList <- function() {
  assertThat(c(1,2,3), identicalTo(c(1, 2, 3)));
}


test.logicals <- function() {
  assertThat(c(TRUE, FALSE, NA), identicalTo(c(TRUE, FALSE, NA)));
}


test.ints <- function() {
  assertThat(c(1L,2L, 3L) , identicalTo(c(1L, 2L, 3L)));
}


test.nullValues <- function() {
  assertThat(c(NULL, NULL), identicalTo(NULL));
}


test.realAndLogicalsMixed <- function() {
  assertThat(c(1,2,NULL,FALSE), identicalTo(c(1, 2, 0)));
}


test.twoLists <- function() {
  assertThat(c( list(1,2), list(3,4) ) , identicalTo(list(1, 2, 3, 4)));
}


test.nullsInList <- function() {
  assertThat(c( list(NULL), NULL, list(NULL,1) ) ,
             identicalTo(list(NULL, NULL, 1)));
}


test.emptyMatrix <- function() {
  print(cbind( matrix(integer(0), nrow=10), integer(0) )) ;
}


test.combineWithExplicitNames <- function() {
  p <- c(x=41,y=42);
  print(p);
  assertThat(p['x'], identicalTo(structure(41, .Names = "x")))
}


test.combineWithExistingNames <- function() {
  x <- c(a=1, b=2, 3);
  y <- c(x, zz=x, 4);

  assertThat(names(y), identicalTo(c("a", "b", "", "zz.a", "zz.b", "zz3", "")));
}


test.genericCombine <- function() {
  c.foo <- function(...) 42 ;
  x <- 1;
  class(x) <- 'foo';

  assertThat(c(x), identicalTo(c(42)));


}


test.genericCombineInterval <- function() {

  c.Interval <- function(...) { args <- list(...); 'FOO' } ;
  reduce <- function(m, check_valid = TRUE) { m; 'REDUCED' } ;
  interval_union <- function(z, ..., check_valid = TRUE) reduce( c(z, ...), check_valid)  ;

  zz <- structure(91, class='Interval');
  interval_union(zz, zz);
}


test.unlistAtomic <- function() {
  assertThat(unlist( list(1,4,5), TRUE, TRUE) , identicalTo(c(1, 4, 5)));
  assertThat(unlist( list(1,'a',TRUE), TRUE, TRUE) , identicalTo(c("1", "a", "TRUE")));
  assertThat(unlist( list(1,globalenv()), TRUE, TRUE), identicalTo(list(1, .GlobalEnv)));
}


test.unlistRaw <- function() {
  assertThat(unlist(list(as.raw(0x1), as.raw(0x33))), identicalTo(as.raw(c(0x01, 0x33))))
}


test.combineRecursively <- function() {
  assertThat(c( list(91,92,c(93,94,95)), 96, c(97,98), recursive=TRUE),
             identicalTo(c(91, 92, 93, 94, 95, 96, 97, 98)));
}


test.combineRecursivelyWithNames <- function() {
  x <- c(a=91,92,c=93);
  y <- c(recursive=TRUE, A=list(p=x,q=x,list(r=3,s=c(1,2,3,4))),B=4,C=x);

  assertThat( names(y) , identicalTo(c("A.p.a", "A.p2", "A.p.c", "A.q.a", "A.q2", "A.q.c", "A.r",
                                               "A.s1", "A.s2", "A.s3", "A.s4", "B", "C.a", "C2", "C.c")));
}


test.pairList <- function() {
  x <- c(pairlist(x=91,y=92)) ;

  assertThat(names(x), identicalTo(c("x", "y")));
  assertThat(length(x), identicalTo(2L));
  assertThat(.Internal(typeof(x)), identicalTo(c("list")));
  assertThat(x[[1]], identicalTo(91));
  assertThat(x[[2]], identicalTo(92));
}


test.rbind_cbind_Simple <- function() {

  x<-rbind(c(Package='survey', Version='3.22-3'))
  assertThat(dim(x), identicalTo(c(1L, 2L)));
  assertThat(dimnames(x), identicalTo(list(NULL, c("Package", "Version"))));
  assertThat(x, identicalTo(structure(c("survey", "3.22-3"), .Dim = 1:2, .Dimnames = list(NULL, c("Package", "Version")))))

  x<-cbind(c(Package='survey', Version='3.22-3'))
  assertThat(dim(x), identicalTo(c(2L, 1L)));
  assertThat(dimnames(x), identicalTo(list(c("Package", "Version"), NULL)))
  assertThat(x, equalTo(c("survey", "3.22-3")))
}


test.cbind_rbind <- function() {

  assertThat(cbind(), identicalTo(NULL));
  assertThat(cbind(5, 6, 7), identicalTo(structure(c(5, 6, 7), .Dim = c(1L, 3L))));
  assertThat(cbind(c(5,6), c(9)), identicalTo(structure(c(5, 6, 9, 9), .Dim = c(2L, 2L))))
  assertThat(cbind(a=c(c=5,d=6), b=c(9)), identicalTo(structure(c(5, 6, 9, 9), .Dim = c(2L, 2L), .Dimnames = list(c("c",  "d"), c("a", "b")))))
  assertThat(cbind(a=1:2, b=3:4), identicalTo(structure(1:4, .Dim = c(2L, 2L), .Dimnames = list(NULL, c("a",  "b")))))

  assertThat(rbind(), identicalTo(NULL));
  assertThat(rbind(5, 6, 7), identicalTo(structure(c(5, 6, 7), .Dim = c(3L, 1L))))
  assertThat(rbind(c(5,6), c(9)), identicalTo(structure(c(5, 9, 6, 9), .Dim = c(2L, 2L))));
  assertThat(rbind(a=c(c=5,d=6), b=c(9)), identicalTo(structure(c(5, 9, 6, 9), .Dim = c(2L, 2L), .Dimnames = list(c("a",  "b"), c("c", "d")))))
  assertThat(rbind(a=1:2, b=3:4), identicalTo(structure(c(1L, 3L, 2L, 4L), .Dim = c(2L, 2L), .Dimnames = list(c("a", "b"), NULL))))
}

test.bindWithDimnames <- function() {
  a <- 1:4;
  dim(a) <- c(4,1) ;
  dimnames(a) <- list(c('r1','r2','r3', 'r4'), c('c1'));
  b <- (1:4)+10;
  dim(b) <- c(4,1) ;

  c <- 1:4;
  dim(c) <- c(1,4) ;
  dimnames(c) <- list(c('c1'), c('r1','r2','r3', 'r4'));
  d <- (1:4)+10;
  dim(d) <- c(1,4) ;

  x <- cbind(a, b)
  y <- rbind(c, d)

  assertThat(length(dimnames(x)[[1]]), identicalTo(4L));
  assertThat(length(dimnames(x)[[2]]), identicalTo(2L));
  assertThat(row.names(x), identicalTo(c("r1","r2","r3","r4")));

  assertThat(length(dimnames(y)[[1]]), identicalTo(2L));
  assertThat(length(dimnames(y)[[2]]), identicalTo(4L));
  assertThat(colnames(y), identicalTo(c("r1","r2","r3","r4")));
}


test.bindAndSubstitute <- function() {
  cbind.foo <- function(..., deparse.level=1) { substitute(...) };
  rbind.foo <- function(..., deparse.level=1) { substitute(...) };

  x <- list(1);
  class(x) <- 'foo';
  a <- cbind(x);
  b <- as.name('x');
  c <- rbind(x);

  assertThat(a, identicalTo(b));
  assertThat(c, identicalTo(b));
}


test.deferredNames <- function() {
  x <- as.double(1:10000);
  y <- c(a=x);
  assertThat(names(y)[1], identicalTo(c("a1")));
  assertThat(names(y)[2], identicalTo(c("a2")));

  x <- as.double(1:10000);
  names(x) <- rep(c('x','y','z'), length=length(x));
  y <- c(a=x);
  assertThat(names(y)[1], identicalTo(c("a.x")));
  assertThat(names(y)[2], identicalTo(c("a.y")));
  assertThat(names(y)[3], identicalTo(c("a.z")));
  assertThat(names(y)[4], identicalTo(c("a.x")));
}


test.bindWithEmpty <- function() {
  x<-1:12;
  dim(x)<-c(3,4);

  assertThat(dim(cbind(x, c())), identicalTo(c(3L, 4L)));
  assertThat(dim(rbind(x, c())), identicalTo(c(3L, 4L)));

  assertThat(dim(cbind(c())), identicalTo(NULL));
  assertThat(dim(rbind(c())), identicalTo(NULL));

}


test.useSymbolNamesAsInBinding <- function() {

  a <- 1:3;
  b <- 4:6;
  c <- 1;
  d <- 2;
  m <- cbind(a,b);
  n <- rbind(a,b);
  o <- rbind(c,d);
  p <- cbind(c,d);

  assertThat( dimnames(m)[[1]], identicalTo(NULL));
  assertThat( dimnames(m)[[2]], identicalTo(c("a", "b")));
  assertThat( dimnames(n)[[1]], identicalTo(c("a", "b")));
  assertThat( dimnames(n)[[2]], identicalTo(NULL));
  assertThat( dimnames(o)[[1]], identicalTo(c("c", "d")));
  assertThat( dimnames(p)[[2]], identicalTo(c("c", "d")));
}


test.BindingNamedVectors <- function() {
  a <- c(C=1,D=2);
  b <- c(C=3,D=4);
  m <- cbind(a,b);
  n <- rbind(a,b);

  assertThat( dimnames(m)[[1]], identicalTo(c("C", "D")));
  assertThat( dimnames(m)[[2]], identicalTo(c("a", "b")));
  assertThat( dimnames(n)[[1]], identicalTo(c("a", "b")));
  assertThat( dimnames(n)[[2]], identicalTo(c("C", "D")));

}


test.BindingWithDeparseLevel <- function() {
  a <- c(C=1,D=2);
  b <- c(C=3,D=4);

  assertThat( dimnames( cbind(a,b,deparse.level=0) )[[1]], identicalTo(c("C", "D")));
  assertThat( dimnames( cbind(a,b,deparse.level=0) )[[2]], identicalTo(NULL));
  assertThat( dimnames( rbind(a,b,deparse.level=0) )[[1]], identicalTo(NULL));
  assertThat( dimnames( rbind(a,b,deparse.level=0) )[[2]], identicalTo(c("C", "D")));

  assertThat( dimnames( cbind(c(C=1,D=2),c(C=3,D=4),deparse.level=0) )[[1]], identicalTo(c("C", "D")));
  assertThat( dimnames( cbind(c(C=1,D=2),c(C=3,D=4),deparse.level=0) )[[2]], identicalTo(NULL));
  assertThat( dimnames( rbind(c(C=1,D=2),c(C=3,D=4),deparse.level=0) )[[1]], identicalTo(NULL));
  assertThat( dimnames( rbind(c(C=1,D=2),c(C=3,D=4),deparse.level=0) )[[2]], identicalTo(c("C", "D")));


  assertThat( dimnames( cbind(a,b,deparse.level=1) )[[1]], identicalTo(c("C", "D")));
  assertThat( dimnames( cbind(a,b,deparse.level=1) )[[2]], identicalTo(c("a", "b")));
  assertThat( dimnames( rbind(a,b,deparse.level=1) )[[1]], identicalTo(c("a", "b")));
  assertThat( dimnames( rbind(a,b,deparse.level=1) )[[2]], identicalTo(c("C", "D")));

  assertThat( dimnames( cbind(c(C=1,D=2),c(C=3,D=4),deparse.level=1) )[[1]], identicalTo(c("C", "D")));
  assertThat( dimnames( cbind(c(C=1,D=2),c(C=3,D=4),deparse.level=1) )[[2]], identicalTo(NULL));
  assertThat( dimnames( rbind(c(C=1,D=2),c(C=3,D=4),deparse.level=1) )[[1]], identicalTo(NULL));
  assertThat( dimnames( rbind(c(C=1,D=2),c(C=3,D=4),deparse.level=1) )[[2]], identicalTo(c("C", "D")));

  assertThat( dimnames( cbind(1:2,3:4,deparse.level=2) )[[1]], identicalTo(NULL));
  assertThat( dimnames( cbind(1:2,3:4,deparse.level=2) )[[2]], identicalTo( c("1:2", "3:4") ));
  assertThat( dimnames( rbind(1:2,3:4,deparse.level=2) )[[1]], identicalTo( c("1:2", "3:4") ));
  assertThat( dimnames( rbind(1:2,3:4,deparse.level=2) )[[2]], identicalTo(NULL));

  assertThat( dimnames( cbind(c(C=1,D=2),c(C=3,D=4),deparse.level=2) )[[1]], identicalTo(c("C", "D")));
  assertThat( dimnames( cbind(c(C=1,D=2),c(C=3,D=4),deparse.level=2) )[[2]], identicalTo(c("c(C = 1, D...", "c(C = 3, D...")));
  assertThat( dimnames( rbind(c(C=1,D=2),c(C=3,D=4),deparse.level=2) )[[1]], identicalTo(c("c(C = 1, D...", "c(C = 3, D...")));
  assertThat( dimnames( rbind(c(C=1,D=2),c(C=3,D=4),deparse.level=2) )[[2]], identicalTo(c("C", "D")));

}


test.bindDispatch <- function() {
  rbind.foo <- function(..., deparse.level = 1) 42L ;
  rbind.bar <- function(..., deparse.level = 1) c(...)*2 ;
  cbind.foo <- function(..., deparse.level = 1) 42L ;
  cbind.bar <- function(..., deparse.level = 1) c(...)*2 ;

  x <- 1;
  class(x) <- 'foo';
  y <- 2;
  z <- 3;
  class(z) <- 'bar';

  assertThat(rbind(x, y), identicalTo(42L));  # WORKS
  assertThat(rbind(y, x), identicalTo(42L));  # WORKS
  assertThat(rbind(x, y, z), equalTo(c(1,2,3))); # default method
  assertThat(rbind(y, z), equalTo(c(4, 6))); # default method

  assertThat(cbind(x, y), identicalTo(42L));
  assertThat(cbind(y, x), identicalTo(42L));
  assertThat(cbind(x, y, z), equalTo(c(1,2,3))); # default method
  assertThat(cbind(y, z), equalTo(c(4, 6))); # default method
}


test.unlistListWithNulls <- function() {
  x <- list(const=NULL,power=NULL);
  y <- unlist(x);

  assertThat(y, identicalTo(NULL));

}

test.handleMultipleNulls <- function() {
  assertThat(cbind(1, NULL, NULL, NULL, c(), c(), NULL, 1), equalTo(c(1, 1)));
  assertThat(dim(cbind(1, NULL, NULL, NULL, c(), c(), NULL, 1)), equalTo(c(1, 2)));
  assertThat(rbind(1, NULL, NULL, NULL, c(), c(), NULL, 1), equalTo(c(1, 1)));
  assertThat(dim(rbind(1, NULL, NULL, NULL, c(), c(), NULL, 1)), equalTo(c(2, 1)));
}
