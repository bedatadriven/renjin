\name{isSorted}

\alias{isSorted}
\alias{isSorted,ANY-method}

\alias{isConstant}
\alias{isConstant,integer-method}
\alias{isConstant,numeric-method}
\alias{isConstant,array-method}

\alias{isStrictlySorted}
\alias{isStrictlySorted,ANY-method}


\title{Test if a vector-like object is sorted}

\description{
  \code{isSorted} and \code{isStrictlySorted} test if a vector-like object
  is sorted or strictly sorted, respectively.

  \code{isConstant} tests if a vector-like or array-like object is constant.
  Currently only \code{isConstant} methods for vectors or arrays of type
  integer or double are implemented.
}

\usage{
isSorted(x)
isStrictlySorted(x)
isConstant(x)
}

\arguments{
  \item{x}{
    A vector-like object. Can also be an array-like object for
    \code{isConstant}.
  }
}

\details{
  Vector-like objects of length 0 or 1 are always considered to be sorted,
  strictly sorted, and constant.

  Strictly sorted and constant objects are particular cases of sorted objects.

  \code{isStrictlySorted(x)} is equivalent to
  \code{isSorted(x) && !anyDuplicated(x)}
}

\value{
  A single logical i.e. \code{TRUE}, \code{FALSE} or \code{NA}.
}

\author{Hervé Pagès}

\seealso{
  \itemize{
    \item \code{\link{is.unsorted}}.

    \item \code{\link{duplicated}} and \code{\link{unique}}.

    \item \code{\link{all.equal}}.

    \item \code{\link{NA}} and \code{\link{is.finite}}.
  }
}

\examples{
## ---------------------------------------------------------------------
## A. isSorted() and isStrictlySorted()
## ---------------------------------------------------------------------

x <- 1:10

isSorted(x)           # TRUE
isSorted(-x)          # FALSE
isSorted(rev(x))      # FALSE
isSorted(-rev(x))     # TRUE

isStrictlySorted(x)   # TRUE

x2 <- rep(x, each=2)
isSorted(x2)          # TRUE
isStrictlySorted(x2)  # FALSE

## ---------------------------------------------------------------------
## B. "isConstant" METHOD FOR integer VECTORS
## ---------------------------------------------------------------------

## On a vector with no NAs:
stopifnot(isConstant(rep(-29L, 10000)))

## On a vector with NAs:
stopifnot(!isConstant(c(0L, NA, -29L)))
stopifnot(is.na(isConstant(c(-29L, -29L, NA))))
 
## On a vector of length <= 1:
stopifnot(isConstant(NA_integer_))


## ---------------------------------------------------------------------
## C. "isConstant" METHOD FOR numeric VECTORS
## ---------------------------------------------------------------------
## This method does its best to handle rounding errors and special
## values NA, NaN, Inf and -Inf in a way that "makes sense".
## Below we only illustrate handling of rounding errors.

## Here values in 'x' are "conceptually" the same:
x <- c(11/3,
       2/3 + 4/3 + 5/3,
       50 + 11/3 - 50,
       7.00001 - 1000003/300000)
## However, due to machine rounding errors, they are not *strictly*
## equal:
duplicated(x)
unique(x)
## only *nearly* equal:
all.equal(x, rep(11/3, 4))  # TRUE

## 'isConstant(x)' uses 'all.equal()' internally to decide whether
## the values in 'x' are all the same or not:
stopifnot(isConstant(x))

## This is not perfect though:
isConstant((x - 11/3) * 1e8)  # FALSE on Intel Pentium paltforms
                              # (but this is highly machine dependent!)
}

\keyword{utilities}
