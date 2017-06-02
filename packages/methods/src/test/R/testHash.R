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

# From the hash package...

# S4 objects which inherit from environments seem to be treated super-specially
# throughout the GNU R code base.

# Their environment is stored in a special .xData slot, and all of the builtin
# and internal functions which apply to environments should also be useable with
# these objects, even though they are of type S4 and not environment.


library(methods)
library(hamcrest)


setClass( 'hash', contains = 'environment' )


hash <- function() {

  # INITIALIZE A NEW HASH
  h <- new(
    "hash" ,
     new.env( hash = TRUE , parent=emptyenv() )
  )
  return(h)
}


x <- hash()

assertThat(typeof(x), identicalTo("S4"))
assertThat(class(x), equalTo("hash"))
assertTrue(is.environment(x))

assertThat(x$a, identicalTo(NULL))

x$b <- 42
assertThat(typeof(x), identicalTo("S4"))
assertThat(x$b, identicalTo(42))
assertThat(x[["b"]], identicalTo(42))

assertThat(x["b"], throwsError())


x[["c"]] <- 43
assertThat(typeof(x), identicalTo("S4"))
assertThat(x$c, identicalTo(43))


# Verify that other builtin functions "just work" on our
# Environment subclass

assertThat(get("c", envir = x), identicalTo(43))
assertThat(get0("c", envir = x), identicalTo(43))

assertTrue("b" %in% ls(x))
assertTrue("c" %in% ls(x))

assertThat(length(x), identicalTo(2L))

# Verify that we can set an element named "NA"
x[[NA_character_]] <- 33
assertTrue("NA" %in% ls(x))
assertThat(x[["NA"]], identicalTo(33))
assertThat(x[[NA_character_]], identicalTo(33))
