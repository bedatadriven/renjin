\name{Rle-runstat}

\alias{Rle-runstat}

\alias{runsum}
\alias{runsum,Rle-method}

\alias{runmean}
\alias{runmean,Rle-method}

\alias{smoothEnds,Rle-method}
\alias{runmed,Rle-method}

\alias{runwtsum}
\alias{runwtsum,Rle-method}

\alias{runq}
\alias{runq,Rle-method}


\title{Fixed-width running window summaries}

\description{
  The \code{runsum}, \code{runmean}, \code{runmed}, \code{runwtsum},
  \code{runq} functions calculate the sum, mean, median, weighted sum,
  and order statistic for fixed width running windows.
}

\usage{
runsum(x, k, endrule = c("drop", "constant"), ...)

runmean(x, k, endrule = c("drop", "constant"), ...)

\S4method{smoothEnds}{Rle}(y, k = 3)

\S4method{runmed}{Rle}(x, k, endrule = c("median", "keep", "drop", "constant"), 
       algorithm = NULL, print.level = 0)

runwtsum(x, k, wt, endrule = c("drop", "constant"), ...)

runq(x, k, i, endrule = c("drop", "constant"), ...)
}

\arguments{
  \item{x}{
    The data object.
  }
  \item{k}{
    An integer indicating the fixed width of the running window. Must be
    odd when \code{endrule != "drop"}.
  }
  \item{endrule}{
    A character string indicating how the values at the beginning
    and the end (of the data) should be treated.
    \describe{
      \item{\code{"median"}}{see \code{\link[stats]{runmed}};}
      \item{\code{"keep"}}{see \code{\link[stats]{runmed}};}
      \item{\code{"drop"}}{do not extend the running statistics to
        be the same length as the underlying vectors;}
      \item{\code{"constant"}}{copies running statistic to the
        first values and analogously for the last ones making the
        smoothed ends \emph{constant}.}
    }
  }
  \item{wt}{
    A numeric vector of length \code{k} that provides the weights to use.
  }
  \item{i}{
    An integer in [0, k] indicating which order statistic to calculate.
  }
  \item{\dots}{Additional arguments passed to methods. Specifically,
    \code{na.rm}. When \code{na.rm = TRUE}, the \code{NA} and \code{NaN}
    values are removed. When \code{na.rm = FALSE}, \code{NA} is returned
    if either \code{NA} or \code{NaN} are in the specified window.
  }
}

\details{
  The \code{runsum}, \code{runmean}, \code{runmed}, \code{runwtsum},
  and \code{runq} functions provide efficient methods for calculating
  the specified numeric summary by performing the looping in compiled code.
}

\value{
  An object of the same class as \code{x}.
}

\author{P. Aboyoun and V. Obenchain}

\seealso{
  \code{\link[stats]{runmed}}, \link{Rle-class}, \link[IRanges]{RleList-class}
}

\examples{
  x <- Rle(1:10, 1:10)
  runsum(x, k = 3)
  runsum(x, k = 3, endrule = "constant")
  runmean(x, k = 3)
  runwtsum(x, k = 3, wt = c(0.25, 0.5, 0.25))
  runq(x, k = 5, i = 3, endrule = "constant")

  ## Missing and non-finite values
  x <- Rle(c(1, 2, NA, 0, 3, Inf, 4, NaN))
  runsum(x, k = 2)
  runsum(x, k = 2, na.rm = TRUE)
  runmean(x, k = 2, na.rm = TRUE)
  runwtsum(x, k = 2, wt =  c(0.25, 0.5), na.rm = TRUE)
  runq(x, k = 2, i = 2, na.rm = TRUE) ## max value in window

  ## The .naive_runsum() function demonstrates the semantics of 
  ## runsum(). This test ensures the behavior is consistent with
  ## base::sum().

  .naive_runsum <- function(x, k, na.rm=FALSE)
      sapply(0:(length(x)-k),
          function(offset) sum(x[1:k + offset], na.rm=na.rm))

  x0 <- c(1, Inf, 3, 4, 5, NA)
  x <- Rle(x0)
  target1 <- .naive_runsum(x0, 3, na.rm = TRUE)
  target2 <- .naive_runsum(x, 3, na.rm = TRUE)
  stopifnot(target1 == target2)
  current <- as.vector(runsum(x, 3, na.rm = TRUE))
  stopifnot(target1 == current)

  ## runmean() and runwtsum() :
  x <- Rle(c(2, 1, NA, 0, 1, -Inf))
  runmean(x, k = 3)
  runmean(x, k = 3, na.rm = TRUE)
  runwtsum(x, k = 3, wt = c(0.25, 0.50, 0.25))
  runwtsum(x, k = 3, wt = c(0.25, 0.50, 0.25), na.rm = TRUE)

  ## runq() :
  runq(x, k = 3, i = 1, na.rm = TRUE) ## smallest value in window
  runq(x, k = 3, i = 3, na.rm = TRUE) ## largest value in window

  ## When na.rm = TRUE, it is possible the number of non-NA 
  ## values in the window will be less than the 'i' specified.
  ## Here we request the 4th smallest value in the window,
  ## which tranlates to the value at the 4/5 (0.8) percentile.
  x <- Rle(c(1, 2, 3, 4, 5))
  runq(x, k=length(x), i=4, na.rm=TRUE)

  ## The same request on a Rle with two missing values
  ## finds the value at the 0.8 percentile of the vector 
  ## at the new length of 3 after the NA's have been removed.
  ## This translates to round((0.8) * 3).
  x <- Rle(c(1, 2, 3, NA, NA))
  runq(x, k=length(x), i=4, na.rm=TRUE)
}

\keyword{methods}
\keyword{algebra}
\keyword{arith}
