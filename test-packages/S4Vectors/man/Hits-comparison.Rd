\name{Hits-comparison}

\alias{Hits-comparison}

\alias{pcompare,Hits,Hits-method}
\alias{match,Hits,Hits-method}
\alias{order,Hits-method}


\title{Comparing and ordering hits}

\description{
  \code{==}, \code{!=}, \code{<=}, \code{>=}, \code{<}, \code{>},
  \code{match()}, \code{\%in\%}, \code{order()}, \code{sort()}, and
  \code{rank()} can be used on \link{Hits} objects to compare and order hits.

  Note that only the \code{"pcompare"}, \code{"match"}, and \code{"order"}
  methods are actually defined for \link{Hits} objects. This is all what is
  needed to make all the other comparing and ordering operations (i.e.
  \code{==}, \code{!=}, \code{<=}, \code{>=}, \code{<}, \code{>},
  \code{\%in\%}, \code{sort()}, and \code{rank()}) work on these objects
  (see \code{?`\link{Vector-comparison}`} for more information about this).
}

\usage{
\S4method{pcompare}{Hits,Hits}(x, y)

\S4method{match}{Hits,Hits}(x, table, nomatch=NA_integer_, incomparables=NULL,
      method=c("auto", "quick", "hash"))

\S4method{order}{Hits}(..., na.last=TRUE, decreasing=FALSE, method=c("shell", "radix"))
}

\arguments{
  \item{x, y, table}{
    \emph{Compatible} \link{Hits} objects, that is, \link{Hits} objects with
    the same subject and query lengths.
  }
  \item{nomatch}{
    The value to be returned in the case when no match is found.
    It is coerced to an \code{integer}.
  }
  \item{incomparables}{
    Not supported.
  }
  \item{method}{
    For \code{match}: Use a Quicksort-based (\code{method="quick"}) or a
    hash-based (\code{method="hash"}) algorithm.
    The latter tends to give better performance, except maybe for some
    pathological input that we've not encountered so far.
    When \code{method="auto"} is specified, the most efficient algorithm will
    be used, that is, the hash-based algorithm if \code{length(x) <= 2^29},
    otherwise the Quicksort-based algorithm.

    For \code{order}: The \code{method} argument is ignored.
  }
  \item{...}{
    One or more \link{Hits} objects. The additional \link{Hits} objects
    are used to break ties.
  }
  \item{na.last}{
    Ignored.
  }
  \item{decreasing}{
    \code{TRUE} or \code{FALSE}.
  }
}

\details{
  Only hits that belong to \link{Hits} objects with same subject and query
  lengths can be compared.

  Hits are ordered by query hit first, and then by subject hit.
  On a \link{Hits} object, \code{order}, \code{sort}, and \code{rank}
  are consistent with this order.

  \describe{
    \item{}{
      \code{pcompare(x, y)}:
      Performs element-wise (aka "parallel") comparison of 2 \link{Hits}
      objects \code{x} and \code{y}, that is, returns an integer vector where
      the i-th element is less than, equal to, or greater than zero if
      \code{x[i]} is considered to be respectively less than, equal to, or
      greater than \code{y[i]}. See \code{?`\link{Vector-comparison}`} for
      how \code{x} or \code{y} is recycled when the 2 objects don't have the
      same length.
    }
    \item{}{
      \code{match(x, table, nomatch=NA_integer_, method=c("auto", "quick", "hash"))}:
      Returns an integer vector of the length of \code{x}, containing the
      index of the first matching hit in \code{table} (or \code{nomatch} if
      there is no matching hit) for each hit in \code{x}.
    }
    \item{}{
      \code{order(...)}:
      Returns a permutation which rearranges its first argument (a \link{Hits}
      object) into ascending order, breaking ties by further arguments (also
      \link{Hits} objects).
    }
  }
}

\author{Hervé Pagès}

\seealso{
  \itemize{
    \item \link{Hits} objects.

    \item \link{Vector-comparison} for general information about comparing,
          ordering, and tabulating vector-like objects.
  }
}

\examples{
## ---------------------------------------------------------------------
## A. ELEMENT-WISE (AKA "PARALLEL") COMPARISON OF 2 Hits OBJECTS
## ---------------------------------------------------------------------
hits <- Hits(c(2, 4, 4, 4, 5, 5), c(3, 1, 3, 2, 3, 2), 6, 3)
hits

pcompare(hits, hits[3])
pcompare(hits[3], hits)

hits == hits[3]
hits != hits[3]
hits >= hits[3]
hits < hits[3]

## ---------------------------------------------------------------------
## B. match(), %in%
## ---------------------------------------------------------------------
table <- hits[-c(1, 3)]
match(hits, table)

hits \%in\% table

## ---------------------------------------------------------------------
## C. order(), sort(), rank()
## ---------------------------------------------------------------------
order(hits)
sort(hits)
rank(hits)
}

\keyword{methods}
