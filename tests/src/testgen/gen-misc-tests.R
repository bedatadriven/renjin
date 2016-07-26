


## Generates test cases for
## functions with higher arity

source("src/testgen/gen.R")

cases <- list(
  
  pretty = list(
      list(x = 1:15),
      list(x = 1:15, h = 2),
      list(x = 1:15, n = 3),
      list(x = 1:15 * 2),
      list(x = 1:20),
      list(x = 1:20, n = 2),
      list(x = 1:20, n = 10),
      list(x = pi),
      list(x = 1.234e100),
      list(x = 1001.1001),
      list(1001.1001, shrink = 0.2))

)


for(fn in names(cases)) {

  # Setup generic implementations
  test <- test.open("gen-misc-tests.R", fn)
  writeln(test, "library(hamcrest)")
  
  tol <- 1e-6
  
  # Check that numerical values are correct
  for(input in cases[[fn]]) {
    writeTest(test, fn, ARGS = input, tol = tol)
  }
  
  close(test)
}

