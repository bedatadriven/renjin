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
    assertTrue( exists("fred", envir = e) )
}

test.redefine <- function() {

    makeActiveBinding("xx", function(val) 41, environment())
    assertThat(xx, identicalTo(41))

    makeActiveBinding("xx", function(val) 42, environment())
    assertThat(xx, identicalTo(42))

}

test.cannot.rebind <- function() {

    rho <- new.env()
    rho$x <- 41

    f <- function(val) 0

    assertThat( { makeActiveBinding("x", f, rho) }, throwsError())
}


test.remove <- function() {

    makeActiveBinding("yy", function(val) 41, environment())
    assertTrue(exists("yy"))
    assertThat(yy, identicalTo(41))

    rm("yy")
    assertFalse(exists("yy"))
}

test.for.loop.index <- function() {

    j <- 4
    ib <- function(val) {
        if(missing(val)) {
            j <<- j + 1
            j
        } else {
            j <<- val * 2
        }
    }

    makeActiveBinding("i", ib, environment())

    sum <- 0
    for(i in 1:10) {
        sum <- sum + i
    }

    assertThat(sum, identicalTo(120))
}

test.sys.frame <- function() {

    y <- 90
    f <- function() print( sys.frame(-1)$y )
    makeActiveBinding("x", f, .GlobalEnv)
    g <- function() {
        y = 91
        x }
    h <- function() {
        y = 92
        g() }
    i <- function() {
        y = 93
        h() }
    assertThat( i(), identicalTo(91) )

    f <- function() print( sys.frame(-2)$y )
    makeActiveBinding("x", f, .GlobalEnv)
    assertThat( i(), identicalTo(92) )

    f <- function() print( sys.frame(-3)$y )
    makeActiveBinding("x", f, .GlobalEnv)
    assertThat( i(), identicalTo(93) )

    f <- function() print( sys.frame(-4)$y )
    makeActiveBinding("x", f, .GlobalEnv)
    assertThat( i(), identicalTo(NULL) )

}

test.sys.call <- function() {
    f <- function() sys.call(-1)
    makeActiveBinding("x", f, .GlobalEnv)
    g <- function() x
    h <- function() g()
    i <- function() h()

    assertThat(i(), identicalTo(quote(g())))
}

test.ellipses.1 <- function() {
    f <- function(val) 42
    g <- function(...) {
        makeActiveBinding("...", f, environment())
        list(...)
    }
    assertThat(g(43), throwsError())
}

test.ellipses.2 <- function() {
    assertThat(makeActiveBinding(sym=`...`, fun=function(...) 42), throwsError())
}

test.import <- function() {
    f <- function(val) 42
    makeActiveBinding("HashMap", f, environment())
    import(java.util.HashMap)

    assertThat(HashMap, identicalTo(42))
}

test.ellipses.3 <- function() {
    f <- function(val) 42
    g <- function(...) {
    	makeActiveBinding("..2", f, environment())
            ..2
    }
    assertThat(g(c("A"),c("B")), identicalTo("B"))
}
