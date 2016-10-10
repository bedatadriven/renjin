\name{str-utils}

\alias{unstrsplit}
\alias{unstrsplit,list-method}
\alias{unstrsplit,character-method}
\alias{safeExplode}
\alias{strsplitAsListOfIntegerVectors}
\alias{svn.time}

\title{Some utility functions to operate on strings}

\description{
  Some low-level string utilities that operate on ordinary
  character vectors. For more advanced string manipulations,
  see the \pkg{Biostrings} package.
}

\usage{
unstrsplit(x, sep="")  # 'sep' default is "" (empty string)

strsplitAsListOfIntegerVectors(x, sep=",")  # 'sep' default is ","
}

\arguments{
  \item{x}{
    For \code{unstrsplit}: A list-like object where each list element is
    a character vector, or a character vector (identity).

    For \code{strsplitAsListOfIntegerVectors}: A character vector where
    each element is a string containing comma-separated decimal integer values.
  }
  \item{sep}{
    A single string containing the separator character.
    For \code{strsplitAsListOfIntegerVectors}, the separator must be a
    single-byte character.
  }
}

\details{
  \subsection{unstrsplit}{
    \code{unstrsplit(x, sep)} is equivalent to (but much faster than)
    \code{sapply(x, paste0, collapse=sep)}. It's performing the reverse
    transformation of \code{\link{strsplit}( , fixed=TRUE)}, that is,
    if \code{x} is a character vector with no NAs and \code{sep} a single
    string, then \code{unstrsplit(strsplit(x, split=sep, fixed=TRUE), sep)}
    is identical to \code{x}. A notable exception to this though is when
    \code{strsplit} finds a match at the end of a string, in which case the
    last element of the output (which should normally be an empty string)
    is not returned (see \code{?strsplit} for the details).
  }

  \subsection{strsplitAsListOfIntegerVectors}{
    \code{strsplitAsListOfIntegerVectors} is similar to the
    \code{strsplitAsListOfIntegerVectors2} function shown in the
    Examples section below, except that the former generally raises
    an error where the latter would have inserted an \code{NA} in
    the returned object. More precisely:
    \itemize{
      \item The latter accepts NAs in the input,
            the former doesn't (raises an error).
      \item The latter introduces NAs by coercion (with a warning),
            the former doesn't (raises an error).
      \item The latter supports "inaccurate integer conversion in coercion"
            when the value to coerce is > INT_MAX (then it's coerced to
            INT_MAX), the former doesn't (raises an error).
      \item The latter coerces non-integer values (e.g. 10.3) to an
            int by truncating them, the former doesn't (raises an error).
    }
    When it fails, \code{strsplitAsListOfIntegerVectors} will print
    an informative error message.
    Finally, \code{strsplitAsListOfIntegerVectors} is faster and uses
    much less memory than \code{strsplitAsListOfIntegerVectors2}.
  }
}

\value{
  \code{unstrsplit} returns a character vector with one string per list
  element in \code{x}.

  \code{strsplitAsListOfIntegerVectors} returns a list where each list
  element is an integer vector. There is one list element per string
  in \code{x}.
}

\author{Hervé Pagès}

\seealso{
  \itemize{
    \item The \code{\link[base]{strsplit}} function in the \pkg{base}
          package.
  }
}

\examples{
## ---------------------------------------------------------------------
## unstrsplit()
## ---------------------------------------------------------------------
x <- list(A=c("abc", "XY"), B=NULL, C=letters[1:4])
unstrsplit(x)
unstrsplit(x, sep=",")
unstrsplit(x, sep=" => ")

data(islands)
x <- names(islands)
y <- strsplit(x, split=" ", fixed=TRUE)
x2 <- unstrsplit(y, sep=" ")
stopifnot(identical(x, x2))

## But...
names(x) <- x
y <- strsplit(x, split="in", fixed=TRUE)
x2 <- unstrsplit(y, sep="in")
y[x != x2]
## In other words: strsplit() behavior sucks :-/

## ---------------------------------------------------------------------
## strsplitAsListOfIntegerVectors()
## ---------------------------------------------------------------------
x <- c("1116,0,-19",
       " +55291 , 2476,",
       "19184,4269,5659,6470,6721,7469,14601",
       "7778889, 426900, -4833,5659,6470,6721,7096",
       "19184 , -99999")

y <- strsplitAsListOfIntegerVectors(x)
y

## In normal situations (i.e. when the input is well-formed),
## strsplitAsListOfIntegerVectors() does actually the same as the
## function below but is more efficient (both in speed and memory
## footprint):
strsplitAsListOfIntegerVectors2 <- function(x, sep=",")
{
    tmp <- strsplit(x, sep, fixed=TRUE)
    lapply(tmp, as.integer)
}
y2 <- strsplitAsListOfIntegerVectors2(x)
stopifnot(identical(y, y2))
}

\keyword{utilities}
