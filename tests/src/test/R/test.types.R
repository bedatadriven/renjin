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

test.asCharacter <- function()  {
  assertThat(as.character(1), identicalTo("1"))
  assertThat(as.character("foobar"), identicalTo("foobar"))
  assertThat(as.character(1L), identicalTo("1"))
  assertThat(as.character(1.3333333333333333333333333333333333), identicalTo("1.33333333333333"))
  assertThat(as.character(TRUE), identicalTo("TRUE"))
  assertThat(as.character(1000), identicalTo("1000"))
}


test.coerceWithoutArgument <- function() {
  assertThat(as.character(), identicalTo(character(0)))
  assertThat(as.double(), identicalTo(numeric(0)))
  assertThat(as.logical(), identicalTo(logical(0)))
  assertThat(as.integer(), identicalTo(integer(0)))
  assertThat(as.complex(), identicalTo(complex(0)))
}

test.doubleNaNToComplex <- function() {
  assertThat(is.na(as.complex(NaN)), identicalTo(TRUE))
  assertThat(is.na(as.complex(0/0)), identicalTo(TRUE))
  assertThat(is.na(as.complex(NA)), identicalTo(TRUE))
}

test.asCharacterWithNA <- function() {
  assertThat(as.character(NA), identicalTo(NA_character_))
}

test.asCharacterFromStringObject <- function() {
  import(java.lang.String)
  x <- String$new("foo")
  assertThat(as.character(x), identicalTo("foo"))
}

test.asCharacterFromList <- function() {
  assertThat(as.character(list(3, 'a', TRUE)), identicalTo(c("3", "a", "TRUE" )))
  assertThat(as.character(list(c(1,3), 'a', TRUE)), identicalTo(c("c(1, 3)", "a", "TRUE" )))
}

test.asCharacterFromSymbol <- function() {
  assertThat(as.character(quote(x)), identicalTo("x"))
}


test.asCharacterFromNull <- function() {
  x<- NULL;
  g<-function(b) b;
  f<-function(a) g(as.character(a)) ;
  assertThat( f(x), identicalTo(character(0)))
}

test.asDoubleFromDoubleObject <- function(){
  import(java.lang.Double);
  x<-Double$new(1.5);
  assertThat(as.double(x), identicalTo(c(1.5)));
}


test.asDoubleFromDouble <- function() {
  assertThat( as.double(3.14), identicalTo(3.14));
  assertThat( as.double(NA_real_), identicalTo(NA_real_ ));
}


test.asDoubleFromInt <- function() {
  assertThat( as.double(3L), identicalTo(3));
}

test.asLogicalFromBooleanObject <- function(){
  import(java.lang.Boolean);
  x<-Boolean$new(TRUE);
  assertThat(as.logical(x), identicalTo(TRUE));
}


test.asLogicalFromList <- function() {
  assertThat( as.logical(list(1, 99.4, 0, 0L, FALSE, 'TRUE', 'FOO', 'T', 'F', 'FALSE')) ,
              identicalTo( c(TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, NA, TRUE, FALSE, FALSE) ));
}


test.asLogical <- function() {
  assertThat( as.logical(c(1, 99.4, 0, NA_real_)) ,
              identicalTo( c(TRUE, TRUE, FALSE, NA) ));
}


test.asLogicalFromString <- function() {
  assertThat( as.logical('TRUE'), identicalTo(TRUE));
  assertThat( as.logical('FALSE'), identicalTo(FALSE));

  assertThat( as.logical('true'), identicalTo(TRUE));
  assertThat( as.logical('false'), identicalTo(FALSE));

  assertThat( as.logical('T'), identicalTo(TRUE));
  assertThat( as.logical('F'), identicalTo(FALSE));

  assertThat( as.logical('TR'), identicalTo(NA));
  assertThat( as.logical('FA'), identicalTo(NA));
}


test.asDoubleFromLogical <- function() {
  assertThat( as.double(TRUE), identicalTo(1))
  assertThat( as.double(FALSE), identicalTo(0))
}


test.asDoubleFromString <- function() {
  assertThat( as.double("42"), identicalTo(42));
  assertThat( as.double("not an integer"), identicalTo(NA_real_));
}


test.asIntFromIntegerObject <- function(){
  import(java.lang.Integer);
  x<-Integer$new(2);
  assertThat(as.integer(x), identicalTo(2L));
}


test.asIntFromDouble <- function() {
  assertThat( as.integer(3.1), identicalTo(3L));
  assertThat( as.integer(3.9), identicalTo(3L));
  assertThat( as.integer(NA_real_), identicalTo(NA_integer_));
}


test.asIntFromRecycledDouble <- function() {
  assertThat( as.integer(c(1, 9.32, 9.9, 5.0)), identicalTo(c(1L, 9L, 9L, 5L)));
}


# test.attributesSetting <- function() {
#   v <- R.Version()
#   attributes(v) <- c( class='simpleList', attributes(v)) ;
#
#   assertThat( v$minor, not(identicalTo(NULL)));
# }


test.attributeReplace <- function() {
  x <- 1:12;
  dim(x) <- c(3,4);
  dim(x) <- c(3,4,1);

  assertThat( dim(x), identicalTo(c(3L,4L,1L)));

  dim(x) <- NULL ;
  assertThat( dim(x), identicalTo(NULL));
}

test.attributeReplaceOnNull <- function() {
  assertThat( { dim(NULL) <- c(0,4) }, throwsError())
}

test.na <- function() {
  assertThat(  is.na(TRUE) , identicalTo( FALSE));
  assertThat(  is.na(NA) , identicalTo( TRUE));
  assertThat(  is.na(c(1L, NA_integer_)) , identicalTo( c(FALSE, TRUE)));
  assertThat(  is.na(c(NA_character_, '', 'foo')) , identicalTo( c(TRUE, FALSE, FALSE)));
}

test.naNULL <- function() {
  assertThat(is.na(c()), identicalTo(logical(0)))
}

test.finite <- function() {
  assertThat( is.finite(42), identicalTo(TRUE));
  assertThat( is.finite(1/0), identicalTo(FALSE));
  assertThat( is.finite(1/0), identicalTo(FALSE));
  assertThat( is.finite(NA), identicalTo(FALSE));
  assertThat( is.finite(NaN), identicalTo(FALSE));
}


test.infinite <- function() {
  assertThat( is.infinite(1), identicalTo(FALSE));
  assertThat( is.infinite(1/0), identicalTo(TRUE));
  assertThat( is.infinite(NA), identicalTo(FALSE));
  assertThat( is.infinite(NaN), identicalTo(FALSE));
}


test.finiteAtomicVectors <- function() {
  assertThat( is.infinite('Inf'), identicalTo(FALSE));
  assertThat( is.finite('Inf'), identicalTo(FALSE));
  assertThat( is.finite(1L), identicalTo(TRUE));
  assertThat( is.finite(TRUE), identicalTo(TRUE));
  assertThat( is.finite(FALSE), identicalTo(TRUE));
  assertThat( is.infinite(TRUE), identicalTo(FALSE));
  assertThat( is.infinite(FALSE), identicalTo(FALSE));
  assertThat( is.infinite(1L), identicalTo(FALSE));
}


test.isnan <- function() {
  assertThat( is.nan(1), identicalTo(FALSE));
  assertThat( is.nan(1/0), identicalTo(FALSE));
  assertThat( is.nan(NA), identicalTo(FALSE));
  assertThat( is.nan(sqrt(-2)), identicalTo(TRUE));
}


test.isNa <- function() {
  assertThat( is.na(1), identicalTo(FALSE));
  assertThat( is.na(NA), identicalTo(TRUE));
  assertThat( is.na(NA_integer_), identicalTo(TRUE));
  assertThat( is.na(NA_character_), identicalTo(TRUE));
  assertThat( is.na(NA_real_), identicalTo(TRUE));
  assertThat( is.na(NA_complex_), identicalTo(TRUE));

  assertThat( is.na(NaN), identicalTo(TRUE));

  assertThat( is.na(1/0), identicalTo(FALSE));
  assertThat( is.na(0/0), identicalTo(TRUE));
}


test.naList <- function() {
  assertThat(  is.na(list(NULL,  1,     FALSE, c(NA,4), NA_integer_, NA_real_)) ,
               identicalTo( c(FALSE, FALSE, FALSE, FALSE,   TRUE,        TRUE)) );
}


test.naPreservesNames <- function() {
  assertThat(  names(is.na(c(x=1,y=2))), identicalTo( c("x", "y")));
}

test.naPreservesDimNames <- function() {
  x <- .Internal(rbind(1, c(a=1,b=2))) ;
  x <- is.na(x) ;
  assertThat(  dimnames(x)[[2]], identicalTo( c("a", "b")))

   x <- !x ;
   assertThat(  dimnames(x)[[2]], identicalTo( c("a", "b")))
}


test.nullDimNamePreservedOnAssignment <- function() {
  x <- 1:12;
  dim(x) <- c(3,4) ;
  dimnames(x) <- list(NULL, c('a','b','c','d'));

  assertThat(dimnames(x)[[1]], identicalTo(NULL));
}


test.unaryPreservesNames <- function() {
  assertThat(  names(!is.na(c(x=1,y=2))), identicalTo( c("x", "y")));
}


test.vector <- function() {
  assertThat(vector('list', 3) , identicalTo( list(NULL, NULL, NULL)));
  assertThat(vector('numeric', 2) , identicalTo( c(0, 0)));
  assertThat(vector('character', 3), identicalTo( c("","","") ))
 assertThat(vector('logical', 2) , identicalTo( c(FALSE, FALSE)) );
}


test.environment <- function() {
  environment <- function(fun=NULL) .Internal(environment(fun)) ;
  f <- function() { qqq<-42; environment()$qqq };
  assertThat( f(), identicalTo(c(42)));

}


test.environmentCalledFromPromise <- function() {
  g <- function(env) env$zz ;
  h <- function() { zz<-33; g(environment()); };
  assertThat( h(), identicalTo(c(33)))
}
test.env2list <- function() {
  env <- new.env(TRUE, globalenv(), 29L)
  env$a <- 1
  env$.a <- 2
  x <- as.list(env,all.names=FALSE)
  y <- as.list(env,all.names=TRUE)

  assertThat(names(x), identicalTo(c("a")))
  assertThat(names(y), identicalTo(c("a", ".a")))
}

test.env2list_hiddenFirst <- function() {
  env <- new.env(TRUE, globalenv(), 29L)
  env$.a <- 1
  env$a <- 2
  x <- as.list(env,all.names=FALSE)
  y <- as.list(env,all.names=TRUE)

  assertThat(names(x), identicalTo(c("a")))
  assertThat(names(y), identicalTo(c("a",".a")))
}

test.env2list_multipleNonHidden <- function() {
  env <- new.env(TRUE, globalenv(), 29L)
  env$a <- 1
  env$b <- 2
  x <- as.list(env,FALSE)
  y <- as.list(env,TRUE)

  assertThat(names(x), identicalTo(c("a","b")))
  assertThat(names(y), identicalTo(c("a","b")))
}

test.environmentName <- function() {
  assertThat( environmentName(baseenv()), identicalTo(c("base")))
  assertThat( environmentName(globalenv()), identicalTo(c("R_GlobalEnv")))
}

test.environmentOfRandomExp <- function() {
  assertThat( environment(1), identicalTo(NULL) )
}


test.environmentOfClosure <- function() {
  f <- function() { 1 } ;
  assertThat( environment(f), identicalTo(environment()))
}

test.list <- function() {
  assertThat( list("a"), identicalTo(list("a")))
}

test.listOfNulls <- function() {
  assertThat( list(NULL, NULL), identicalTo( list(NULL, NULL) ));
}

test.listOfNull <- function() {
  assertThat( list(NULL), identicalTo( list(NULL) ));
}


test.closureBody <- function() {
  f <- function(x) sqrt(x) ;

  assertThat( body(f)[[1]], identicalTo(as.symbol("sqrt")))
}


test.setClassWithAttrFunction <- function() {
  x<-c(1,2,3) ;
  attr(x, 'class') <- 'foo' ;

  assertThat(  class(x), identicalTo(c("foo")));
}

#
#  test.asFunctionDefault <- function() {
#    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
#    list.add("a", Symbol.MISSING_ARG);
#    list.add("b", new DoubleArrayVector(2));
#    list.add(FunctionCall.newCall(Symbol.get("+"), Symbol.get("a"), Symbol.get("b")));
#    global.setVariable(topLevelContext, Symbol.get("x"), list.build());
#
#    f <- .Internal(as.function.default(x, globalenv()));
#    assertThat(f(1), identicalTo(c(3)));
#    assertThat(f(1,3), identicalTo(c(4)));
#  }


test.dimAttributesAreConverted <- function() {
  x <- 1;
  attributes(x) <- list(dim=1);
}


test.atomicVectorsHaveImplicitClasses <- function() {
  assertThat( class(9), identicalTo(c("numeric")))
  assertThat( class(9L), identicalTo(c("integer")))
  assertThat( class('foo'), identicalTo(c("character")))
  assertThat(class(TRUE), identicalTo(c("logical")))
  assertThat(class(NULL), identicalTo(c("NULL")))
}


#  @Ignore("to implement")
#  someSpecialFunctionsHaveTheirOwnImplicitClass <- function() {
#    assertThat( class(quote({1}))"), identicalTo(c("{));
#    assertThat( class(quote(if(TRUE) 1 else 0))"), identicalTo(c("if));
#    assertThat( class(quote(while(TRUE) 1))"), identicalTo(c("while));
#    assertThat( class(quote(for(x in 1:9) x))"), identicalTo(c("for));
# //   assertThat( class(quote(x=1)"), equalTo(c("=));
#    assertThat( class(quote(x<-1)"), identicalTo(c("<-));
#    assertThat( class(quote((1+1))"), identicalTo(c("());
#  }


test.implicitClassesAreOverridenByClassAttribute <- function() {
  m <- 1:12;
  dim(m) <- c(3,4);
  class(m) <- c('foo','bar');
  assertThat( class(m), identicalTo(c("foo", "bar")))
}

test.matricesHaveImplicitClass <- function() {
  m <- 1:12;
  dim(m) <- c(3,4);
  assertThat( class(m), identicalTo(c("matrix")));
}

test.matricesAreNotObjects <- function() {
  m <- 1:12;
  dim(m) <- c(3,4);
  assertThat( is.object(m), identicalTo(FALSE));
}


test.arraysHaveImplicitClass <- function() {
  a <- 1:12;
  dim(a) <- 12;
  assertThat( class(a), identicalTo(c("array")));
}

test.unclass <- function() {
  x<-1;
  class(x) <- 'foo';
  x <- unclass(x);
  assertThat(class(x), identicalTo(c("numeric")));
}


test.unclassPreservesOtherAttribs <- function() {
  x<-1;
  attr(x,'zing')<-'bat';
  class(x) <- 'foo';
  x <- unclass(x);
  assertThat(class(x), identicalTo(c("numeric")));
  assertThat(attr(x,'zing'), identicalTo(c("bat")));
}


setNamesWithNonStrVector <- function() {
  x<-c(1,2,3) ;
  names(x) <- c(4,5,6) ;

  assertThat( names(x), identicalTo( c("4", "5","6")))
}


test.setNamesWithNonVector <- function() {
  x<-c(1,2,3) ;
  names(x) <- quote(quote(z)) ;

  assertThat( names(x), identicalTo( c("z", NA_character_, NA_character_) ))
}


test.setAttributes <- function() {
  x <- 1:5;
  attributes(x) <- list(names=c('a','b', 'c'), foo='bar') ;

  assertThat( names(x) , identicalTo(c("a", "b", "c", NA, NA)));
  assertThat(  attr(x, 'foo'), identicalTo( c("bar")));
}

#
#  test.asEnvironment <- function() {
#    assertThat( as.environment(1), sameInstance((SEXP) topLevelContext.getGlobalEnvironment()));
#    assertThat( as.environment(2), sameInstance((SEXP) topLevelContext.getGlobalEnvironment().getParent()));
#  }


test.asVector <- function() {
  assertThat( as.vector(1, 'character'), identicalTo( c("1")));
  assertThat( as.vector(c(4,5,0), mode='logical'), identicalTo(c(TRUE, TRUE, FALSE)));
  assertThat( as.vector(c(TRUE,FALSE,NA), mode='double'), identicalTo(c(1.0, 0, NA_real_)));
}

test.asVectorDropsNames <- function() {
  x <- c(Intercept=1, x=2);
  attr(x, 'foo') <- 'bar';
  y <- as.vector(x);

  assertThat( attributes(y), identicalTo(NULL))
}

test.asVectorPreservesNamesForLists <- function() {
  x <- c(a=1, b=2, c=3);
  attr(x, 'foo') <- 'bar';

  y <- as.vector(x, 'list') ;

  # Names are preserved
  assertThat(names(y), identicalTo(c("a", "b", "c")));

  # all other attributes are discarded
  assertThat(attr(y, 'foo'), identicalTo(NULL))
}

test.asVectorPreservesNamesForPairListsButNothingElse <- function() {
  x <- c(a=1, b=2, c=3);
  attr(x, 'foo') <- 'bar';

  y <- as.vector(x, 'pairlist') ;

  # Names are preserved
  assertThat(names(y), identicalTo(c("a", "b", "c")))

  # all other attributes are saved
  assertThat(attr(y, 'foo'), identicalTo(NULL));
}


test.pairListAsPairListPreservesAllAttributes <- function() {
  x <- pairlist(1,2,3,4);
  dim(x) <- c(2,2) ;
  attr(x, 'foo') <- 'bar';

  y <- as.vector(x, 'pairlist');
  assertThat(dim(y), identicalTo(c(2L, 2L)));
  assertThat(attr(y, 'foo'), identicalTo(c("bar")));


  y <- as.pairlist(x) ;
  assertThat(dim(y), identicalTo(c(2L, 2L)));
  assertThat(attr(y, 'foo'), identicalTo(c("bar")));
}

test.asVectorDoesNotPreserveAttributesForLists <- function() {
  x <- 1:12;
  dim(x) <- c(2,6);
  rownames(x) <- c('a', 'b');

  y <- as.vector(x, 'list') ;

  # Dims and dimnames are NOT preserved
  assertThat(dim(y), identicalTo(NULL));
  assertThat(dimnames(y), identicalTo(NULL));
}

test.naSymbol <- function() {
  s <- as.vector('NA', 'symbol');
  assertThat(s, identicalTo(quote(`NA`)))
}


test.zeroLengthSymbol <- function() {
  assertThat(as.vector('', 'symbol'), throwsError())
}

test.asPairList <- function() {
  x <- as.vector( c(a=1,b=2), mode = 'pairlist') ;

  #    PairList.Node head = (PairList.Node) global.getVariable(topLevelContext, "x");
  #    assertThat( head.length(), equalTo(2));
  #    assertThat( head.getNode(0).getTag(), identicalTo( symbol("a")));
  #    assertThat( head.getElementAsSEXP(0), identicalTo( c(1) ));
  #    assertThat(head.getNode(1).getTag(), identicalTo( symbol("b") ));
  #    assertThat( head.getElementAsSEXP(1), identicalTo( c(2) ));
}

test.pairListToList <- function() {

  x <- as.vector(list(a=41, b=42), 'pairlist')
  y <- as.vector(x, 'list');

  assertThat( y, identicalTo( list(41,42)));
  assertThat( names(x), identicalTo( c("a", "b")));
  assertThat( typeof(x), identicalTo( c("pairlist")));
assertThat( names(y), identicalTo( c("a", "b")))
}

test.functionCallToList <- function() {

  x <- quote(~(0+births))
  y <- as.vector(x, 'list')

  assertThat( length(y), identicalTo(2L));
  assertThat( names(y), identicalTo(  NULL ));
  assertThat( typeof(y[[2]]), identicalTo("language"))
}

test.setLength <- function(){
  x <- c(1,2,3);
  assertThat(length(x), identicalTo(3L));
  length(x)<-4;
  assertThat(length(x), identicalTo(4L));
  assertThat(is.na(x[4]), identicalTo(TRUE));

  length(x) <- 2;
  assertThat(x, identicalTo(c(1,2)));
}


test.setLengthOnOneDimensionalNamedArray <- function() {
  a <- 1:3;
  dim(a) <- 3;
  dimnames(a) <- list(letters[1:3]);

  length(a) <- 4;

  assertThat(dim(a), identicalTo(NULL));
  assertThat(names(a), identicalTo(c("a", "b", "c", "")));
}


test.setLengthWithNames <- function() {
  x <- c(a=1,b=2,c=3);
  attr(x, 'foo') <- 'baz';
  y = c(a=1, b=2, c=3);
  attr(y, 'foo') <- 'bar';

  length(x)<-2;
  length(y)<-3;
  assertThat(length(x), identicalTo(2L));
  assertThat(length(names(x)), identicalTo(2L));
  assertThat(attr(x,'foo'), identicalTo(NULL));

  assertThat(length(y), identicalTo(3L));
  assertThat(length(names(y)), identicalTo(3L));
  assertThat(attr(y,'foo'), identicalTo(c("bar")));
}


test.isRawAndAsRaw <- function(){
  assertThat( is.raw(as.raw(c(123,124))), identicalTo(TRUE));
  assertThat( as.raw(c(1,20,30)), identicalTo(as.raw(c(0x1, 0x14, 0x1e))))
}


test.rawToBits <- function(){
  assertThat( rawToBits(as.raw(c(1,2))), identicalTo(
    as.raw(c(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))))
}


test.charToRaw <- function(){
  assertThat( charToRaw("ABC"), identicalTo(as.raw(c(0x41, 0x42, 0x43))))
}


test.multiByteCharToRaw <- function(){
  assertThat(charToRaw('\u00a0'), identicalTo(as.raw(c(0xc2, 0xa0))))
}


test.rawShift <- function() {
  assertThat(rawShift(as.raw(c(29:31)),1), identicalTo(as.raw(c(0x3a, 0x3c, 0x3e))))
}


test.intToBits <- function() {
  assertThat(intToBits(1), identicalTo(as.raw(c(0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00))))
  assertThat(intToBits(234234), identicalTo(as.raw(c(0x00, 0x01, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00,  0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00))))
  assertThat(intToBits(NA), identicalTo(as.raw(c(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x01))))

}

test.isNaGeneric <- function() {

  x<-1;
  class(x) <- 'foo';

  is.na.foo <- function(x) 'FOO!!';
  assertThat(is.na(x), identicalTo(c("FOO!!")));
}

test.rawToChar <- function() {
  assertThat(rawToChar(as.raw(32:126), FALSE), identicalTo(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"))
}


test.drop <- function() {

  x <- 1:12;
  dim(x) <- c(3,1, 4, 1);
  dimnames(x) <- list(c('r1','r2','r3'), 'd2', c('c1', 'c2', 'c3', 'c4'), 'd4');
  y <- drop(x)

  assertThat(y, identicalTo(structure(1:12, .Dim = 3:4, .Dimnames = list(c("r1", "r2", "r3" ), c("c1", "c2", "c3", "c4")))))
}

test.expression <- function() {
  ex <- expression({ x * 2});
  x<-4 ;
  assertThat( eval(ex), identicalTo(8));
}

test.getThrowsOnNonExistantVariable <- function() {
  assertThat(get('nonexistant.variable', globalenv(), 'any', TRUE), throwsError())
}


test.existsNoInherit <- function() {

  x <- 42;
  f <- function() { exists('x', inherits=FALSE) } ;
  assertThat( f(), identicalTo(FALSE));
}


test.getNoInheritThrows <- function() {
  x <- 42;
  f <- function() { exists('x', inherits=FALSE) } ;
  f();
}


test.listToEnvironment <- function() {
  x <- as.environment(list(a=42,b=64));
  assertThat(x$a, identicalTo(c(42)));
  assertThat(x$b, identicalTo(c(64)));

}


test.asEnvironmentWithName <- function() {
  assertThat(as.environment('package:base'), identicalTo(baseenv()))
}


test.asGlobalEnv <- function() {
  f <- function() as.environment('.GlobalEnv');
  environment(f) <- .BaseNamespaceEnv ;

  f()
}


test.convertingDoubleVector <- function() {
  as.numeric(as.character(1:10));
}


test.asListDoesntDropsDimension <- function() {
  x <- list(1,2,3,4,5,6)
  dim(x) <- 2:3

  assertThat(as.list(x), identicalTo(structure(list(1, 2, 3, 4, 5, 6), .Dim = 2:3)))
  assertThat(as.vector(x, 'list'), identicalTo(structure(list(1, 2, 3, 4, 5, 6), .Dim = 2:3)))
}

test.assignAttributesNull <- function() {

  x <- NULL
  attributes(x) <- NULL
  assertTrue(is.null(x))

  y <- NULL
  attributes(y) <- list(foo="bar")

  assertThat(y, identicalTo(structure(list(), foo = "bar")))
}
