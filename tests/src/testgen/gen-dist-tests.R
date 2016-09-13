


## Generates test cases for
## probability distributions

source("src/testgen/gen.R")

## Declare distributions and some 
## reasonable values for their parameters
dists <- list(
  beta = list(
    shape1 = c(0.5, 1, 2, 3, 5), 
    shape2 = c(0.5, 1, 2, 3, 5)
  ),
  binom = list(
    size = c(0, 1, 5, 10, 20),
    prob = c(0, 0.1, 0.2, 0.5, 1.0)
  ),
  cauchy = list(
    location = c(0, -2),
    scale = c(0, 0.5, 1, 2)
  ),
  chisq = list(
    df = c(1, 2, 3, 4)
  ),
  exp = list(
    scale = c(0, 0.5, 1.0, 1.5)
  ),
  f = list(
    df1 = c(1, 2, 5, 10),
    df2 = c(1, 2, 5, 10)
  )
)

tol <- 0.0001

for(dist in names(dists)) {

  ## Test Random generation functions first
  rfn <- sprintf("r%s", dist)
  test <- test.open("gen-dist-tests.R", rfn)
  writeln(test, "library(hamcrest)")
  writeFixture(test, "set.seed(1)")
  
  params <- dists[[dist]]
  
  writeTest(test, rfn, ARGS = c(list(n = 1), params), tol = tol)
  writeTest(test, rfn, ARGS = c(list(n = 1:5), params), tol = tol)
  writeTest(test, rfn, ARGS = c(list(n = 15), params), tol = tol)
  writeTest(test, rfn, ARGS = c(list(n = numeric(0)), params), tol)

  params.with.na <- params
  params.with.na[[1]][1] <- NA
  writeTest(test, rfn, ARGS = c(list(n = 3), params.with.na))

  
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

