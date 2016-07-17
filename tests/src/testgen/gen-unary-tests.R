


## Generates test cases for
## miscellaneous unary functions

source("src/testgen/gen.R")

fns <- c('as.array',
         'as.call',
         'as.character',
         'as.complex',
         'as.double',
         'as.expression',
         'as.factor',
         'as.integer',
         'as.list',
         'as.logical',
         'as.matrix',
         'as.name',
         'as.numeric',
         'as.ordered',
         'as.pairlist',
         'as.raw',
         'as.single',
         'as.symbol',
         'as.vector',
         'is.array',
         'is.atomic',
         'is.call',
         'is.character',
         'is.complex',
         'is.double',
         'is.element',
         'is.environment',
         'is.expression',
         'is.factor',
         'is.finite',
         'is.function',
         'is.infinite',
         'is.integer',
         'is.language',
         'is.list',
         'is.loaded',
         'is.logical',
         'is.matrix',
         'is.na',
         'is.name',
         'is.nan',
         'is.null',
         'is.numeric',
         'is.object',
         'is.ordered',
         'is.pairlist',
         'is.primitive',
         'is.R',
         'is.raw',
         'is.recursive',
         'is.single',
         'is.symbol',
         'is.table',
         'is.unsorted',
         'is.vector',
         'is.na',
         'length')


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
  signif(1:5 * pi),
  signif(1:5 * -pi),
  c(a = 1L, b = 2L),  # names
  c(a = 1.5, b = 2.5), 

  # character
  character(0),
  c("4.1", "blahh", "99.9", "-413", NA),

  # lists
  list(1, 2, 3),
  list(1, 2, NULL),
  list(1L, 2L, 3L),
  list(1L, 2L, NULL),
  list(1, 2, list(3, 4)),
  
  # matrices
  matrix(1:12, nrow = 3), 
  matrix(1:12, nrow = 3, dimnames = list(x=letters[1:3], y=letters[4:7])),  
  structure(1:3, rando.attrib=941L),

  #arrays
  array(1:3, dim = 3L, dimnames = list(c("a", "b", "c"))),
  
  # S3 dispatch?
  structure(list("foo"), class="foo"),
  structure(list("bar"), class="foo"),
  
  # Symbols
  as.name("xyz"),
  
  # Function Calls
  call("sin", 3.14)
)


for(fn in fns) {

  # Setup generic implementations
  test <- test.open("gen-unary-tests.R", fn)
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

