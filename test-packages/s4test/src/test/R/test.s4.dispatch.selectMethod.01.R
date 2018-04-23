#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

setClass("Matrix", representation(x="numeric"))
setClass("sparseMatrix", contains = "Matrix")
frm = structure(1:5, .Dim = c(5L, 1L))
to = new("sparseMatrix", x = 1)


# method selection standardGeneric
setGeneric("add", function(from, to) standardGeneric("add"))
setMethod("add", signature(from="matrix", to="Matrix"), function(from, to) 100)
setMethod("add", signature(from="ANY", to="sparseMatrix"), function(from, to) 200)
SM = selectMethod("add", c("matrix", "sparseMatrix"))

assertThat(SM(frm, to), identicalTo(100))


# "coerce" special case
setMethod("coerce", signature(from="matrix", to="Matrix"), function(from, to) 100)
setMethod("coerce", signature(from="ANY", to="sparseMatrix"), function(from, to) 200)
SM = selectMethod("coerce", c("matrix", "sparseMatrix"))

assertThat(SM(frm, to), identicalTo(200))


# define useInherited
SM = selectMethod("add", c("matrix", "sparseMatrix"), useInherited = c(TRUE, FALSE))

assertThat(SM(frm, to), identicalTo(200))


