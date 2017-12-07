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
l <- list(first=1.0, second=list(2.0, list(3.0, "zzz")), third="qqq", fourth=list("aaa", "bbb"))
llist <- list(first=-2, second=list(-4, list(-6, 123)), third=123, fourth=list(123, 123))
lreplace <- list(first=-2, second=list(-4, list(-6, "zzz")), third="qqq", fourth=list("aaa", "bbb"))
lunlist <- c(-2, -4, -6, 123, 123, 123, 123)
f <- function(x) -2*x
clz <- c("double")
test.rapply.1 <- function() assertThat(rapply(l, f, clz, deflt=123, how="list"), identicalTo(llist))
test.rapply.2 <- function() assertThat(rapply(l, f, clz, how="replace"), identicalTo(lreplace))
test.rapply.1 <- function() assertThat(rapply(l, f, clz, deflt=123, how="unlist"), identicalTo(lunlist))
