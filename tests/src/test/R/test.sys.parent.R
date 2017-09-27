
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


library(hamcrest)

## Define some helper functions...

f <- function(n) { fn <- 'f'; sys.parent(n); }
g <- function(n) { fn <- 'g'; f(n); }
h <- function(n) { fn <- 'h'; g(n); }


## When called from the top-level context, sys.parent always returns 0

n <- sys.parent();    assertThat(n, identicalTo(0L))
n <- sys.parent(-1);  assertThat(n, identicalTo(0L))
n <- sys.parent(-2);  assertThat(n, identicalTo(0L))
n <- sys.parent(1);   assertThat(n, identicalTo(0L))
n <- sys.parent(2);   assertThat(n, identicalTo(0L))
n <- sys.parent(300); assertThat(n, identicalTo(0L))

# Calling h() creates the following call stack:
# 0: TOP
# 1: h()
# 2: g()
# 3: f()
# 4: sys.parent()

# Should return the number of the parent frame relative to f()

h0 <- h(0);  assertThat(h0, identicalTo(3L))  # f
h1 <- h(1);  assertThat(h1, identicalTo(2L))  # g
h2 <- h(2);  assertThat(h2, identicalTo(1L))  # h
h3 <- h(3);  assertThat(h3, identicalTo(0L))  # TOP
h4 <- h(4);  assertThat(h4, identicalTo(0L))  # TOP



