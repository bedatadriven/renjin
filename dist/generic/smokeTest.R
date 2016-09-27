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


# Smoke test for generic distribution

# ensure that stats package is loaded by default
x <- rnorm(12)

# ensure that C/fortran routines are compiling
dim(x) <- c(3,4)
invisible(qr(x))

# lm() should at least work!
x <- 1:30
y <- x*2
m <- lm(y ~ x)
stopifnot( abs(m$coefficients["x"]-2) < 0.0001 )


# Also ensure that we can dynamically load packages 
# and their dependencies

library(org.renjin.test.alpha)
stopifnot(identical(alphaVersion(), "2.5.1"))

# Verify that parsing multi-line statements works...
v1 <- 91; v2 <- 9; v3 <- 10;
stopifnot(v3 == 10)

# And that we can exit cleanly...
q()