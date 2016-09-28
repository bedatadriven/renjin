#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
  ),
  unif = list(
    min = c(1, 2, 3, 4, 5.5),
    max = c(6, 10, 15, 125.4, 8)
  ),
  norm = list(
    mean = c(10, 20, 30, 55.5),
    sd = c(2.5, 5, 8, 0.5, 8.8)
  ),
  gamma = list(
    shape = c(0.1, 0.3, 1),
    rate = c(1, 2, 3, 4)
  ),
  pois = list(
    lambda = c(1, 3, 5.5, 0)
  ),
  signrank = list(
    n = c(5, 0.5, 0, 0.4)
  ),
  wilcox = list(
    m = c(3, 0.5, 0),
    n = c(5, 0, 0.5, 1)
  ),
  geom = list(
    prob = c(0.1, 0.5, 1)
  ),
  t = list(
    df = c(0, 0.5, 5, 1),
    ncp = c(0, 0.5, 5, -5)
  ),
  lnorm = list(
    meanlog = c(0, 0.5, 5),
    sdlog = c(0, 1, 0.5, 5)
  ),
  logis = list(
    location = c(0, 0.5, 5),
    scale = c(0, 1, 0.5, 5)
  ),
  weibull = list(
    shape = c(0.1, 0.5, 5),
    scale = c(0, 1, 0.5, 5)
  ),
  nbinom = list(
    size = c(0.1, 1, 5),
    prob = c(0.1, 0.5)
  ),
  hyper = list(
    m = c(3, 2.5, 3),
    n = c(5, 3, 9.5),
    k = c(2, 3.5, 0)
  ),
  multinom = list(
    size = c(10, 5, 1, 0),
    prob = c(0, 0.5, 1)
  )
)

for(dist in names(dists)) {

  ## Test Random generation functions first
  rfn <- sprintf("r%s", dist)
  test <- test.open("gen-dist-tests.R", rfn)
  writeln(test, "library(hamcrest)")
  writeln(test, "library(stats)")
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

