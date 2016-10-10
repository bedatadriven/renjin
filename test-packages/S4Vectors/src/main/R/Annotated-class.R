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

### =========================================================================
### Annotated objects
### -------------------------------------------------------------------------

setClass("Annotated", representation("VIRTUAL", metadata = "list"))

setGeneric("metadata", function(x, ...) standardGeneric("metadata"))
setMethod("metadata", "Annotated",
          function(x) {
              if (is.null(x@metadata) || is.character(x@metadata))
                  list(metadata = x@metadata)
              else
                  x@metadata
          })

setGeneric("metadata<-",
           function(x, ..., value) standardGeneric("metadata<-"))
setReplaceMethod("metadata", "Annotated",
                 function(x, value) {
                     if (!is.list(value))
                         stop("replacement 'metadata' value must be a list")
                     if (!length(value))
                         names(value) <- NULL # instead of character()
                     x@metadata <- value
                     x
                 })
