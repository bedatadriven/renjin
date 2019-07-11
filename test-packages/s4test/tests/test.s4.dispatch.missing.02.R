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

setClass("aa", slots = c(x = 'numeric'))
a1 = new("aa", x = 0)

setMethod("[<-", signature(x = "aa", i = "missing", j = "ANY", value = "ANY"),
    function(x, i, j, value) {
        res <- fun1(x, i, j, value)
        return(res)
    })

setMethod("[<-", signature(x = "aa", i = "missing", j = "ANY", value = "character"),
    function(x, i, j, value) "aa[missing, ANY] <- character")

"fun1" <- function(x, i, j, value) {
    list(x@x, j, value)
}

test.missing.1 = function() {
    a1[ , 1] <- 2
    assertThat(deparse(a1), identicalTo("list(0, 1, 2)"))
}
