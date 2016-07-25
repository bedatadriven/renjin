
## Generates test cases for combining functions

source("src/testgen/gen.R")

unary <- c('cbind', 'rbind')

inputs <- list(
  NULL,
  
  # Vectors
  integer(0),
  structure(integer(0), .Names=character(0)),
  1L,
  c(1L, 2L, 3L),
  c(a=1L, b=2L, c=3L),
  
  # 1-d arrays
  array(1:3, dim = 3L),
  array(1:3, dim = 3L, dimnames = list(letters[1:3])),
  array(1:3, dim = 3L, dimnames = list(x = letters[1:3])),
  
  # 2-d matrices
  matrix(1:12, nrow = 3),
  matrix(1:12, nrow = 3, dimnames = list(letters[1:3], letters[4:7])),
  matrix(1:12, nrow = 3, dimnames = list(x = letters[1:3], y = letters[4:7]))
)


for(fn in unary) {
  
  # Setup generic implementations
  test <- test.open("generate-bind-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  # define some nonsense generic functions
  writeFixture(test, "%s.foo <- function(x) 41", fn)
  writeFixture(test, "Math.bar <- function(x) 44")
  
  # First try with no arguments
  writeTest(test, fn)
  
  # Now with one argument
  for(vector in inputs) {
    writeTest(test, fn, vector)
  }
  
  # Now with two arguments
  for(i in inputs) {
    for(j in inputs) {
      writeTest(test, fn, i, j)
    }
  }
  
  # Now with three arguments
  for(i in inputs) {
    for(j in inputs) {
      for(k in inputs) {
        writeTest(test, fn, i, j, k)
      }
    }
  }
  
  close(test)
}
