#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
library(methods)

setGeneric("doIt", function(x, i, j, ..., value) standardGeneric("doIt"))
setMethod("doIt", signature = c(x = "numeric", i = "ANY", j = "ANY", value = "ANY"), function(x, i, j, ..., value) {
    res <- list()
    res$input <- c(x, i, j, value)
    res$inputEllipses <- list(...)

    .local = function(lx, li, lvalue) {
        res$dotLocal <<- c(lx, li, lvalue)
        res$dotLocalEllipses <<- list(...)
    }
    .local(x, i, j)
    return(res)
})


x = doIt(x=10, i=20, j=30, q=33, z=55, value=100)

assertThat(x$input, identicalTo(c(10,20,30,100)))
assertThat(names(x$inputEllipses), identicalTo(c("q","z")))
assertThat(x$inputEllipses$q, identicalTo(33))
assertThat(x$inputEllipses$z, identicalTo(55))

assertThat(x$dotLocal, identicalTo(c(10,20,30)))
assertThat(names(x$inputEllipses), identicalTo(c("q","z")))
assertThat(x$inputEllipses$q, identicalTo(33))
assertThat(x$inputEllipses$z, identicalTo(55))

