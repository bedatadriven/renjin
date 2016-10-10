\name{aggregate-methods}

\alias{aggregate-methods}

\alias{aggregate}
\alias{aggregate,matrix-method}
\alias{aggregate,data.frame-method}
\alias{aggregate,ts-method}
\alias{aggregate.Vector}
\alias{aggregate,Vector-method}
\alias{aggregate,Rle-method}
\alias{aggregate,List-method}

\title{Compute summary statistics of subsets of vector-like objects}

\description{
  The \pkg{S4Vectors} package defines \code{\link[stats]{aggregate}} methods
  for \link{Vector}, \link{Rle}, and \link{List} objects.
}

\usage{
\S4method{aggregate}{Vector}(x, by, FUN, start=NULL, end=NULL, width=NULL,
          frequency=NULL, delta=NULL, ..., simplify=TRUE)

\S4method{aggregate}{Rle}(x, by, FUN, start=NULL, end=NULL, width=NULL,
          frequency=NULL, delta=NULL, ..., simplify=TRUE)

\S4method{aggregate}{List}(x, by, FUN, start=NULL, end=NULL, width=NULL,
          frequency=NULL, delta=NULL, ..., simplify=TRUE)
}

\arguments{
  \item{x}{
    A \link{Vector}, \link{Rle}, or \link{List} object.
  }
  \item{by}{
    An object with \code{\link[BiocGenerics]{start}},
    \code{\link[BiocGenerics]{end}}, and \code{\link[BiocGenerics]{width}}
    methods.

    If \code{x} is a \link{List} object, the \code{by} parameter can be a
    \link[IRanges]{RangesList} object to aggregate within the list elements
    rather than across them. When \code{by} is a \link[IRanges]{RangesList}
    object, the output is either a \link[IRanges]{SimpleAtomicList} object,
    if possible, or a \link{SimpleList} object, if not.
  }
  \item{FUN}{
    The function, found via \code{match.fun}, to be applied to each subset
    of \code{x}.
  }
  \item{start, end, width}{
    The start, end, and width of the subsets. If \code{by} is missing, then
    two of the three must be supplied and have the same length.
  }
  \item{frequency, delta}{
    Optional arguments that specify the sampling frequency and increment
    within the subsets (in the same fashion as \code{\link[stats]{window}}
    from the \pkg{stats} package does).
  }
  \item{...}{
    Optional arguments to \code{FUN}.
  }
  \item{simplify}{
    A logical value specifying whether the result should be simplified
    to a vector or matrix if possible.
  }
}

\details{
  Subsets of \code{x} can be specified either via the \code{by} argument
  or via the \code{start}, \code{end}, \code{width}, \code{frequency}, and
  \code{delta} arguments.

  For example, if \code{start} and \code{end} are specified, then:
\preformatted{
  aggregate(x, FUN=FUN, start=start, end=end, ..., simplify=simplify)
}
  is equivalent to:
\preformatted{
  sapply(seq_along(start),
         function(i) FUN(x[start[i]:end[i]], ...), simplify=simplify)
}
  (replace \code{x[start[i]:end[i]]} with 2D-style subsetting
  \code{x[start[i]:end[i], ]} if \code{x} is a \link{DataFrame} object).
}

\seealso{
  \itemize{
    \item The \code{\link[stats]{aggregate}} function in the \pkg{stats}
          package.

    \item \link{Vector}, \link{Rle}, \link{List}, and \link{DataFrame} objects.

    \item The \code{\link[BiocGenerics]{start}},
          \code{\link[BiocGenerics]{end}}, and
          \code{\link[BiocGenerics]{width}} generic functions defined in
          the \pkg{BiocGenerics} package.
  }
}

\examples{
x <- Rle(10:2, 1:9)
aggregate(x, x > 4, mean)
aggregate(x, FUN=mean, start=1:26, width=20)

## Note that aggregate() works on a DataFrame object the same way it
## works on an ordinary data frame:
aggregate(DataFrame(state.x77), list(Region=state.region), mean)
aggregate(weight ~ feed, data=DataFrame(chickwts), mean)

library(IRanges)
by <- IRanges(start=1:26, width=20, names=LETTERS)
aggregate(x, by, is.unsorted)
}

\keyword{methods}
\keyword{utilities}
