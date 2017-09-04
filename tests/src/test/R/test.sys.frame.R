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

f <- function(n) { fn <- 'f'; sys.frame(n); }
g <- function(n) { fn <- 'g'; f(n); }
h <- function(n) { fn <- 'h'; g(n); }

## sys.frame(0) ALWAYS returns the
## *global* environment, which is different behavior than sys.function and sys.call

assertThat(f(0), identicalTo(.GlobalEnv))
assertThat(g(0), identicalTo(.GlobalEnv))
assertThat(h(0), identicalTo(.GlobalEnv))


## sys.frame(n) when n > 0, then we count down the call stack from the top

# 0:top -> 1:f(1)
f1 <- f(1); assertThat(f1$fn, identicalTo('f'))

# 0:top -> 1:g -> 2:f
g1 <- g(1); assertThat(g1$fn, identicalTo('g'))
g2 <- g(2); assertThat(g2$fn, identicalTo('f'))

# 0:top -> 1:h -> 2:g -> 3:f
h1 <- h(1); assertThat(h1$fn, identicalTo('h'))
h2 <- h(2); assertThat(h2$fn, identicalTo('g'))
h3 <- h(3); assertThat(h3$fn, identicalTo('f'))


## sys.frame(n) when n < 0, then we count upfrom the current stack

# 0:top -> 1:f(1)
fm1 <- f(-1); assertThat(fm1, identicalTo(.GlobalEnv))

# 0:top -> 1:g -> 2:f
gm1 <- g(-1); assertThat(gm1$fn, identicalTo('g'))
gm2 <- g(-2); assertThat(gm2,    identicalTo(.GlobalEnv))

# 0:top -> 1:h -> 2:g -> 3:f
hm1 <- h(-1); assertThat(hm1$fn, identicalTo('g'))
hm2 <- h(-2); assertThat(hm2$fn, identicalTo('h'))
hm3 <- h(-3); assertThat(hm3,    identicalTo(.GlobalEnv))
