

## Generates test cases for
## Complex group tests

source("src/testgen/gen.R")

fns <- c('Re', 'Im', 'Mod', 'Arg', 'Conj')
            
inputs <- list(
  NULL,

  # logical
  logical(0),
  c(TRUE, TRUE, FALSE, FALSE, TRUE),
  c(a=TRUE, FALSE),
  c(TRUE, FALSE, NA),
 
  # integer
  integer(0),
  structure(integer(0), .Names = character(0)),
  c(1L, 2L, 3L),
  c(1L, NA, 4L, NA, 999L),
  c(1L, 2L, 1073741824L, 1073741824L), # overflow
  
  # double
  double(0),
  NaN,
  NA_real_,
  1,
  1.5,
  -1.5,
  3.14,
  Inf,
  -Inf,
  
  # complex
  complex(0),
  structure(complex(0), .Names = character(0)),
  NaN+3i,
  NA_complex_,
  +1.5+3i,
  +1.5-3i,
  -1.5+3i,
  +1.5-3i,
  c(a=3+4i, b=9+2i),
  structure(5+9i, rando.attr = 'bazinga', class = 'zarg'),
  
  # character
  character(0),
  c('4.1', 'blahh', '99.9', '-413', NA),

  # lists
  list(1),
  
  # matrices
  matrix(1:12, nrow = 3), 
  matrix(1:12, nrow = 3, dimnames = list(x=letters[1:3], y=letters[4:7])),  
  structure(1:3, rando.attrib=941L),

  #arrays
  array((1:3)+4.5i, dim = 3L, dimnames = list(c('a', 'b', 'c'))),
  
  # S3 dispatch?
  structure(list('foo'), class='foo'),
  structure(list('bar'), class='foo')
)


for(fn in fns) {

  # Setup generic implementations
  test <- test.open("gen-unary-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  # define some nonsense generic functions
  writeFixture(test, "%s.foo <- function(...) 41", fn)
  writeFixture(test, "Complex.bar <- function(...) 45")

  tol <- 0.0001
  
  # Check that numerical values are correct
  for(input in inputs) {
    writeTest(test, fn, input, tol = tol)
  }
  
  # Check S3 dispatch
  writeTest(test, fn, structure("foo", class='foo'))
  writeTest(test, fn, structure(list(1L, "bar"), class='bar'))
  
  # Remove symbols we added to the global environment
  rm(list = c(sprintf("%s.foo", fn), "Complex.bar"))
  
  close(test)
}

