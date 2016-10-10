\name{split-methods}

\alias{split-methods}
\alias{split}
\alias{split,Vector,ANY-method}
\alias{split,ANY,Vector-method}
\alias{split,Vector,Vector-method}
\alias{split,list,Vector-method}

\title{Divide a vector-like object into groups}

\description{
  \code{split} divides the data in a vector-like object \code{x} into the
  groups defined by \code{f}.

  NOTE: This man page is for the \code{split} methods defined in the
  \pkg{S4Vectors} package. See \code{?base::\link[base]{split}} for the
  default method (defined in the \pkg{base} package).
}

\usage{
\S4method{split}{Vector,ANY}(x, f, drop=FALSE)

\S4method{split}{ANY,Vector}(x, f, drop=FALSE)

\S4method{split}{Vector,Vector}(x, f, drop=FALSE)

\S4method{split}{list,Vector}(x, f, drop=FALSE, ...)
}

\arguments{
  \item{x, f}{
    2 vector-like objects of the same length. \code{f} will typically be a
    factor, but not necessarily.
  }
  \item{drop}{
    Logical indicating if levels that do not occur should be dropped (if
    \code{f} is a factor).
  }
  \item{...}{
    Arguments passed to \code{base::\link{split}} (see Details below).
  }
}

\details{
  The first 3 methods just delegate to the
  \code{IRanges::\link[IRanges]{splitAsList}} function defined in the
  \pkg{IRanges} package.

  The method for \code{list} does:
\preformatted{    split(x, as.vector(f), drop=drop, ...)
}
}

\value{
  All these methods behave like \code{base::\link{split}} except that the
  first 3 methods return a \link{List} object instead of an ordinary list.
}

\seealso{
  \itemize{
    \item The \code{\link[base]{split}} function in the \pkg{base} package.

    \item The \code{\link[IRanges]{splitAsList}} function in the \pkg{IRanges}
          package.

    \item \link{Vector} and \link{List} objects.

    \item \link{Rle} and \link{DataFrame} objects.
  }
}

\examples{
## On an Rle object:
x <- Rle(101:105, 6:2)
split(x, c("B", "B", "A", "B", "A"))

## On a DataFrame object:
groups <- c("group1", "group2")
DF <- DataFrame(
    a=letters[1:10],
    i=101:110,
    group=rep(factor(groups, levels=groups), c(3, 7))
)
split(DF, DF$group)
}

\keyword{manip}
