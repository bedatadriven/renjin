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

test.makeActiveBinding <- function() {

    e <- new.env()
    f <- function(v) {
      if(!missing(v)) m <<- v
      m
    }
    g <- function(w) {
      if(!missing(w)) o <<- w
      o
    }
    makeActiveBinding("fred", f, e)
    makeActiveBinding("gred", g, e)

    e$fred <- 1 + 1
    x <- e$fred
    e$fred <- 2 + 2
    y = e$fred
    assign("fred", 100, envir = e)
    assign("gred", 100, envir = e)
    e$gred <- 200

    assertTrue( x != y )
    assertTrue( y > x )
    assertTrue( e$fred == 100 )
    assertTrue( get("fred", e) == 100 )
    assertTrue( mget(c("fred", "gred"), e)[[1]] == 100 )
    assertTrue( names(mget(c("fred", "gred"), e))[2] == c("gred") )
    assertTrue( ls(e)[2] == c("gred") )

}

