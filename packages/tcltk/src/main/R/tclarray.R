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

tclArray <- function() {
    x <- tclVar()
    tcl("unset", x)
    tcl("array", "set", x, "")
    class(x) <- c(class(x), "tclArray")
    x
}

"[[.tclArray" <- function(x, ...) {
    name <- as.character(x)
    i <- paste(...,sep=",")
    rval <- .External(.C_RTcl_GetArrayElem, name, i)
    if (!is.null(rval)) class(rval) <- "tclObj"
    rval
}

"[[<-.tclArray" <- function(x, ..., value){
    name <- as.character(x)
    i <- paste(..., sep=",")
    if (is.null(value))
        .External(.C_RTcl_RemoveArrayElem, name, i)
    else {
        value <- as.tclObj(value)
        .External(.C_RTcl_SetArrayElem, name, i, value)
    }
    x
}

"$.tclArray" <- function(x, i) {
    name <- as.character(x)
    i <- as.character(i)
    rval <- .External(.C_RTcl_GetArrayElem, name, i)
    if (!is.null(rval)) class(rval) <- "tclObj"
    rval
}

"$<-.tclArray" <- function(x, i, value){
    name <- as.character(x)
    i <- as.character(i)
    if (is.null(value))
        .External(.C_RTcl_RemoveArrayElem, name, i)
    else {
        value <- as.tclObj(value)
        .External(.C_RTcl_SetArrayElem, name, i, value)
    }
    x
}

names.tclArray <- function(x)
    as.character(tcl("array", "names", x))

"names<-.tclArray" <- function(x, value)
    stop("cannot change names on Tcl array")

length.tclArray <- function(x)
    as.integer(tcl("array", "size", x))

"length<-.tclArray" <- function(x, value)
    stop("cannot set length of Tcl array")



