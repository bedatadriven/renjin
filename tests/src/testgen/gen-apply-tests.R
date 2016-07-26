
## Generates test cases for combining functions

source("src/testgen/gen.R")

inputs <- list(
  integer(0),
  1:15,
  
  character(0),
  letters[1:4],
  c(x = "a", y = "b"),
  
  list(),
  list(a = 1, b = 2),
  
  matrix(1:12, nrow=3)
  
)

applyfns <- c("lapply", "sapply")

for(applyfn in applyfns) {
  
  # Setup generic implementations
  test <- test.open("generate-apply-tests.R", applyfn)
  writeln(test, "library(hamcrest)")
  
  # define some functions which we can use
  fns <- c("f1", "f2", "f3")
  writeFixture(test, "f1 <- function(x) x")
  writeFixture(test, "f2 <- function(x) x*2")
  writeFixture(test, "f3 <- function(x) NULL")
  
  for(input in inputs) {
    for(fun in fns) {
      writeTest(test, applyfn, input, fun)
    }
  }
  
  close(test)
}
