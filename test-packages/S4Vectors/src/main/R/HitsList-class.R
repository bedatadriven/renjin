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
### HitsList objects
### -------------------------------------------------------------------------

### FIXME: Rename this class SimpleHitsList and make HitsList a virtual
### class that SimpleHitsList (and possibly CompressedHitsList, defined in
### IRanges) extend directly.
setClass("HitsList",
    contains="SimpleList",
    representation(
        subjectOffsets="integer"
    ),
    prototype=prototype(elementType="Hits")
)

setClass("SelfHitsList",
    contains="HitsList",
    prototype=prototype(elementType="SelfHits")
)

setClass("SortedByQueryHitsList",
    contains="HitsList",
    prototype=prototype(elementType="SortedByQueryHits")
)

setClass("SortedByQuerySelfHitsList",
    contains=c("SelfHitsList", "SortedByQueryHitsList"),
    prototype=prototype(elementType="SortedByQuerySelfHits")
)


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Accessors
###

setGeneric("space", function(x, ...) standardGeneric("space"))

setMethod("space", "HitsList",
          function(x) {
            space <- names(x)
            if (!is.null(space))
              space <-
                rep.int(space, sapply(as.list(x, use.names = FALSE), length))
            space
          })

setMethod("from", "HitsList", function(x) {
  as.matrix(x)[,1L,drop=TRUE]
})

setMethod("to", "HitsList", function(x) {
  as.matrix(x)[,2L,drop=TRUE]
})


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Constructor
###

### This constructor always returns a SortedByQueryHitsList instance at the
### moment.
### TODO: Maybe add the 'sort.by.query' argument to let the user choose
### between getting a HitsList or SortedByQueryHitsList instance.
HitsList <- function(list_of_hits, subject)
{
  subjectOffsets <- c(0L, head(cumsum(sapply(subject, length)), -1))
  subjectToQuery <- seq_along(list_of_hits)
  if (!is.null(names(list_of_hits)) && !is.null(names(subject)))
    subjectToQuery <- match(names(list_of_hits), names(subject))
  subjectOffsets <- subjectOffsets[subjectToQuery]
  new_SimpleList_from_list("SortedByQueryHitsList", list_of_hits,
                           subjectOffsets = subjectOffsets)
}


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Going from Hits to HitsList with extractList() and family.
###

setMethod("relistToClass", "Hits",
    function(x) "HitsList")

setMethod("relistToClass", "SortedByQueryHits",
    function(x) "SortedByQueryHitsList")


### - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
### Coercion
###

## return as.matrix as on Hits, with indices adjusted

setMethod("as.matrix", "HitsList", function(x) {
  mats <- lapply(x, as.matrix)
  mat <- do.call(rbind, mats)
  rows <- c(0L, head(cumsum(sapply(x, nLnode)), -1))
  nr <- sapply(mats, nrow)
  mat + cbind(rep.int(rows, nr), rep.int(x@subjectOffsets, nr))
})

## count up the matches for each left node in every matching

setMethod("as.table", "HitsList", function(x, ...) {
  counts <- unlist(lapply(x, as.table))
  as.table(array(counts, length(counts), list(range = seq_along(counts))))
})

setMethod("t", "HitsList", function(x) {
  x@elements <- lapply(as.list(x, use.names = FALSE), t)
  x
})

### TODO: many convenience methods

