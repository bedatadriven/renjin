\name{shiftApply-methods}

\alias{shiftApply-methods}

\alias{shiftApply}
\alias{shiftApply,Vector,Vector-method}
\alias{shiftApply,vector,vector-method}

\title{Apply a function over subsequences of 2 vector-like objects}

\description{
  \code{shiftApply} loops and applies a function overs subsequences
  of vector-like objects \code{X} and \code{Y}.
}

\usage{
shiftApply(SHIFT, X, Y, FUN, ..., OFFSET=0L, simplify=TRUE, verbose=FALSE)
}

\arguments{
  \item{SHIFT}{A non-negative integer vector of shift values.}
  \item{X, Y}{The vector-like objects to shift.}
  \item{FUN}{The function, found via \code{match.fun}, to be
    applied to each set of shifted vectors.}
  \item{...}{Further arguments for \code{FUN}.}
  \item{OFFSET}{A non-negative integer offset to maintain throughout
    the shift operations.}
  \item{simplify}{A logical value specifying whether or not the
    result should be simplified to a vector or matrix if possible.}
  \item{verbose}{A logical value specifying whether or not to
    print the \code{i} indices to track the iterations.}
}

\details{
  Let \code{i} be the indices in \code{SHIFT},
  \code{X_i = window(X, 1 + OFFSET, length(X) - SHIFT[i])}, and
  \code{Y_i = window(Y, 1 + SHIFT[i], length(Y) - OFFSET)}.
  \code{shiftApply} calculates the set of \code{FUN(X_i, Y_i, ...)} values
  and returns the results in a convenient form.
}

\seealso{
  \itemize{
    \item The \code{\link[S4Vectors]{window}} and
          \code{\link[S4Vectors]{aggregate}} methods for vector-like
          objects defined in the \pkg{S4Vectors} package.

    \item \link{Vector} and \link{Rle} objects.
  }
}

\examples{
set.seed(0)
lambda <- c(rep(0.001, 4500), seq(0.001, 10, length = 500),
            seq(10, 0.001, length = 500))
xRle <- Rle(rpois(1e7, lambda))
yRle <- Rle(rpois(1e7, lambda[c(251:length(lambda), 1:250)]))

cor(xRle, yRle)
shifts <- seq(235, 265, by=3)
corrs <- shiftApply(shifts, yRle, xRle, FUN=cor)

cor(xRle, yRle)
shiftApply(249:251, yRle, xRle,
           FUN=function(x, y) var(x, y) / (sd(x) * sd(y)))
}

\keyword{methods}
\keyword{utilities}
