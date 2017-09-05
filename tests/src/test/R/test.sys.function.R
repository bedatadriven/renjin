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


# Frame numbers are integer that counts from 0 at the
# top-level context, and increments each time a function call starts

fn1 <- function() sys.nframe()
fn2 <- function() fn1()
fn3 <- function() fn2()

n0 <- sys.nframe()
n1 <- fn1()
n2 <- fn2()
n3 <- fn3()

assertThat(n0, identicalTo(0L))
assertThat(n1, identicalTo(1L))
assertThat(n2, identicalTo(2L))
assertThat(n3, identicalTo(3L))

# sys.function(n) returns...
# IF n == 0: the current frame
# IF n > 0:  the frame at depth n (where the TOP=0)
# IF n < 0:  the frame at parent abs(n)

g0 <- function(n) sys.function(n)
g1 <- function(n) g0(n)
g2 <- function(n) g1(n)

# sys.function(0) ALWAYS returns the
# current frame, which will be g0

assertThat(g0(0), identicalTo(g0))
assertThat(g1(0), identicalTo(g0))
assertThat(g2(0), identicalTo(g0))

# or NULL if invoked from the top-level

top <- sys.function(0)

assertThat(top, identicalTo(NULL))

# sys.function(1..n) will count DOWN from
# the top-level context.

# So sys.function(1) is the first function call down,
# regardless of the depth from which it's called.

f0 <- g0(1)   # 0:TOP -> 1:g0
f1 <- g1(1)   # 0:TOP -> 1:g1 -> 2:g0
f2 <- g2(1)   # 0:TOP -> 1:g2 -> 2:g1 -> 3:g0

assertThat(f0, identicalTo(g0))
assertThat(f1, identicalTo(g1))
assertThat(f2, identicalTo(g2))

# sys.function(2) is the next function down the call stack
f1 <- g1(2)   # 0:TOP -> 1:g1 -> 2:g0
f2 <- g2(2)   # 0:TOP -> 1:g2 -> 2:g1 -> 3:g0

assertThat(f1, identicalTo(g0))
assertThat(f2, identicalTo(g1))

# Negative numbers count *UP* from the current frame.
f0 <- g0(-1)   # 0:TOP -> 1:g0
f1 <- g1(-1)   # 0:TOP -> 1:g1 -> 2:g0
f2 <- g2(-1)   # 0:TOP -> 1:g2 -> 2:g1 -> 3:g0

assertThat(f0, identicalTo(NULL))
assertThat(f1, identicalTo(g1))
assertThat(f2, identicalTo(g1))

f1 <- g1(-2)   # 0:TOP -> 1:g1 -> 2:g0
f2 <- g2(-2)   # 0:TOP -> 1:g2 -> 2:g1 -> 3:g0

assertThat(f1, identicalTo(NULL))
assertThat(f2, identicalTo(g2))


# Note that sys.function(-n) starts counting up from where the function call to
# sys.function() was defined, not where it is ultimately evaluated.

f <- function(x) x
g <- function(n) f(sys.function(n))

assertThat(g(0), identicalTo(g))

gm1 <- g(-1)
assertThat(gm1, identicalTo(NULL))

# If we count to far up or too far down, an error is thrown
assertThat(sys.function(1000), throwsError())
assertThat(sys.function(-1000), throwsError())

# When called from within eval(), there is a special
# .Primitive("eval") function that appears magically.

e0 <- eval(quote(sys.function(0)))
em1 <- eval(quote(sys.function(-1)))
em2 <- eval(quote(sys.function(-2)))

assertThat(typeof(e0), identicalTo("builtin"))  # magical .Primitive("eval")
assertThat(em1, identicalTo(eval))              # wrapping closure "eval"
assertThat(em2, identicalTo(NULL))              # top-level context



