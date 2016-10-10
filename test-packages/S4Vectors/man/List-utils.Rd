\name{List-utils}

\alias{List-utils}

\alias{lapply,List-method}
\alias{sapply,List-method}

\alias{endoapply}
\alias{endoapply,list-method}
\alias{endoapply,data.frame-method}
\alias{endoapply,List-method}

\alias{revElements}
\alias{revElements,list-method}
\alias{revElements,List-method}

\alias{mendoapply}
\alias{mendoapply,list-method}
\alias{mendoapply,data.frame-method}
\alias{mendoapply,List-method}

\alias{pc}

\alias{Reduce,List-method}
\alias{Filter,List-method}
\alias{Find,List-method}
\alias{Map,List-method}
\alias{Position,List-method}

\alias{within,List-method}


\title{Common operations on List objects}

\description{
  Various functions and methods for looping on \link{List} objects,
  functional programming on \link{List} objects, and evaluation of an
  expression in a \link{List} object.
}

\usage{
## Looping on List objects:
## ------------------------

\S4method{lapply}{List}(X, FUN, ...)

\S4method{sapply}{List}(X, FUN, ..., simplify=TRUE, USE.NAMES=TRUE)

endoapply(X, FUN, ...)

revElements(x, i)

mendoapply(FUN, ..., MoreArgs=NULL)

pc(...)

## Functional programming methods for List objects:
## ------------------------------------------------

\S4method{Reduce}{List}(f, x, init, right=FALSE, accumulate=FALSE)
\S4method{Filter}{List}(f, x)
\S4method{Find}{List}(f, x, right=FALSE, nomatch=NULL)
\S4method{Map}{List}(f, ...)
\S4method{Position}{List}(f, x, right=FALSE, nomatch=NA_integer_)

## Evaluation of an expression in a List object:
## ---------------------------------------------

\S4method{within}{List}(data, expr, ...)
}

\arguments{
  \item{X, x}{
    A list, data.frame or \link{List} object.
  }
  \item{FUN}{
    The function to be applied to each element of \code{X}
    (for \code{endoapply}) or for the elements in \code{...}
    (for \code{mendoapply}).
  }
  \item{...}{
    For \code{lapply}, \code{sapply}, and \code{endoapply}, optional
    arguments to \code{FUN}.

    For \code{mendoapply}, a set of list, data.frame or \link{List}
    objects to compute over.

    For \code{pc}, one or more list-like objects.

    For \code{Map}, one or more \link{List} objects.
    (FIXME: Mixing \link{List} objects with ordinary lists doesn't seem
    to work properly at the moment.)
  }
  \item{simplify, USE.NAMES}{
    See \code{?base::\link[base]{sapply}} for a description of
    these arguments.
  }
  \item{MoreArgs}{
    A list of other arguments to \code{FUN}.
  }
  \item{i}{
    Index specifying the elements to replace. Can be anything supported
    by \code{`[<-`}.
  }
  \item{f, init, right, accumulate, nomatch}{
    See \code{?base::\link[base]{Reduce}} for a description of
    these arguments.
  }
  \item{data}{
    A \link{List} object.
  }
  \item{expr}{
    Expression to evaluate.
  }
}

\details{
  \subsection{Looping on List objects}{
    Like the standard \code{\link[base]{lapply}} function defined in the
    \pkg{base} package, the \code{lapply} method for \link{List} objects
    returns a list of the same length as \code{X}, with each element being
    the result of applying \code{FUN} to the corresponding element of \code{X}.

    Like the standard \code{\link[base]{sapply}} function defined in the
    \pkg{base} package, the \code{sapply} method for \link{List} objects
    is a user-friendly version of \code{lapply} by default returning a vector
    or matrix if appropriate.

    \code{endoapply} and \code{mendoapply} perform the endomorphic equivalents
    of \code{\link[base]{lapply}} and \code{\link[base]{mapply}} by returning
    objects of the same class as the inputs rather than a list.

    \code{revElements(x, i)} reverses the list elements in \code{x} specified
    by \code{i}. It's equivalent to, but faster than, doing
    \code{x[i] <- endoapply(x[i], rev)}.

    \code{pc(...)} combines list-like objects in an element-wise fashion.
    It's similar to, but faster than, \code{mapply(c, ..., SIMPLIFY=FALSE)}.
    With the following differences:
    \enumerate{
      \item \code{pc()} ignores the supplied objects that are NULL.
      \item \code{pc()} does not recycle its arguments. All the supplied
            objects must have the same length.
      \item If one of the supplied objects is a \link{List} object, then
            \code{pc()} returns a \link{List} object.
      \item \code{pc()} always returns a homogenous list or \link{List} object,
            that is, an object where all the list elements have the same type.
    }
  }
  \subsection{Functional programming methods for List objects}{
    The R base package defines some higher-order functions that are commonly
    found in Functional Programming Languages.
    See \code{?base::\link[base]{Reduce}} for the details, and, in particular,
    for a description of their arguments.
    The \pkg{S4Vectors} package provides methods for \link{List} objects, so,
    in addition to be an ordinary vector or list, the \code{x} argument can
    also be a \link{List} object.
  }
  \subsection{Evaluation of an expression in a List object}{
    \code{within} evaluates \code{expr} within \code{as.env(data)} via
    \code{eval(data)}. Similar to \code{with}, except assignments made
    during evaluation are taken as assignments into \code{data}, i.e.,
    new symbols have their value appended to \code{data}, and assigning
    new values to existing symbols results in replacement.
  }
}

\value{
  \code{endoapply} returns an object of the same class as \code{X},
  each element of which is the result of applying \code{FUN} to the
  corresponding element of \code{X}.

  \code{mendoapply} returns an object of the same class as the first
  object specified in \code{\dots}, each element of which is the result
  of applying \code{FUN} to the corresponding elements of \code{\dots}.

  \code{pc} returns a list or List object of the same length as the
  input objects.

  See \code{?base::\link[base]{Reduce}} for the value returned by the
  functional programming methods.

  See \code{?base::\link[base]{within}} for the value returned by
  \code{within}.
}

\author{P. Aboyoun and H. Pagès}

\seealso{
  \itemize{
    \item The \link{List} class.

    \item \code{base::\link[base]{lapply}} and \code{base::\link[base]{mapply}}
          for the default \code{lapply} and \code{mapply} methods.

    \item \code{base::\link[base]{Reduce}} for the default functional
          programming methods.

    \item \code{base::\link[base]{within}} for the default \code{within}
          method.
  }
}

\examples{
a <- data.frame(x = 1:10, y = rnorm(10))
b <- data.frame(x = 1:10, y = rnorm(10))

endoapply(a, function(x) (x - mean(x))/sd(x))
mendoapply(function(e1, e2) (e1 - mean(e1)) * (e2 - mean(e2)), a, b)

x <- list(a=11:13, b=26:21, c=letters)
y <- list(-(5:1), c("foo", "bar"), 0.25)
pc(x, y)

library(IRanges)
x <- IntegerList(a=11:13, b=26:21, c=31:36, d=4:2)
y <- NumericList(-(5:1), 1:2, numeric(0), 0.25)
pc(x, y)

Reduce("+", x)

Filter(is.unsorted, x)

pos1 <- Position(is.unsorted, x)
stopifnot(identical(Find(is.unsorted, x), x[[pos1]]))

pos2 <- Position(is.unsorted, x, right=TRUE)
stopifnot(identical(Find(is.unsorted, x, right=TRUE), x[[pos2]]))

y <- x * 1000L
Map("c", x, y)
}

\keyword{utilities}
\keyword{methods}
