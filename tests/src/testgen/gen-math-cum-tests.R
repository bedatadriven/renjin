
## Generates test cases for
## the cumxxx functions in the math group

source("src/testgen/gen.R")

unary <- c('cumsum', 'cumprod', 'cummax', 'cummin')

inputs <- list(
  c(TRUE, TRUE, FALSE, FALSE, TRUE),
  c(1L, 2L, 3L),
  c(1L, NA, 4L, NA, 999L),
  signif(1:5 * pi),
  signif(1:5 * -pi),
  c(1L, 2L, 1073741824L, 1073741824L), # overflow
  c(a = 1L, b = 2L),  # names
  c(a = 1.5, b = 2.5), 
  matrix(1:12, nrow = 3),
  c("4.1", "blahh", "99.9", "-413", NA),
  structure(1:3, rando.attrib=941L),
  array(1:3, dim = 3L, dimnames = list(c("a", "b", "c"))),
  numeric(0),
  integer(0),
  structure(integer(0), .Names = character(0))
 # TODO: GNU R does not seem to handle this correctly...  as.raw(c(0, 255, 31))
)


for(fn in unary) {

  # Setup generic implementations
  test <- test.open(fn)
  writeln(test, "library(hamcrest)")
  
  # define some nonsense generic functions
  writeln(test, "%s.foo <- function(x) 41", fn)
  writeln(test, "Math.bar <- function(x) 44")
  
  # define the generic functions in the current environment 
  # as well so we produce the right values for comparison
  assign(sprintf("%s.foo",fn), function(x) 41)
  assign(sprintf("Math.bar",fn), function(x) 44)
  
  tol <- 0.0001
  
  # Check that numerical values are correct
  for(input in inputs) {
    writeTest(test, fn, input, tol = tol)
  }
  
  # Check S3 dispatch
  writeTest(test, fn, structure("foo", class='foo'))
  writeTest(test, fn, structure(list(1L, "bar"), class='bar'))
  
  
  close(test)
}

run.test <- function() {
    for(f in ls(envir = .GlobalEnv)) {
      if(grepl(f, pattern="^test\\.")) {
        print(f)
        do.call(f, list())
      } 
    }
}

