
## Generates test cases for combining functions

source("src/testgen/gen.R")

fns <- c('colSums', 'rowSums', 'colMeans', 'rowMeans')

inputs <- list(
  c(1L, 2L, 3L),
  
  # 2-d matrices 
  matrix(1:12, nrow = 3),
  matrix(1:12, nrow = 3, dimnames = list(letters[1:3], letters[4:7])),
  matrix(1:12, nrow = 3, dimnames = list(x = letters[1:3], y = letters[4:7])),

  # 2-d matrices with NAs
  matrix(c(1, NA, NA, 3), nrow = 2),
  matrix(c(1, NA, NA, 3), nrow = 2, dimnames = list(c("r1", "r2"), c("c1", "c2"))),
  matrix(c(1, NA, NA, 3), nrow = 2, dimnames = list(x = c("r1", "r2"), y = c("c1", "c2"))),
  
  # zero-length 2-d matrices
  matrix(1:3, nrow = 3),
  matrix(1:3, nrow = 3, dimnames = list(letters[1:3], character(0))),
  matrix(1:3, nrow = 3, dimnames = list(x = letters[1:3], y = character(0)))
)


for(fn in fns) {
  
  # Setup generic implementations
  test <- test.open(fn)
  writeln(test, "library(hamcrest)")
  
  # default na.rm
  for(input in inputs) {
    writeTest(test, fn, input, tol = 1e-6)
  }
  
  # na.rm = TRUE
  for(input in inputs) {
    writeTest(test, fn, input, na.rm = TRUE, tol = 1e-6)
  }
  
  close(test)
}
