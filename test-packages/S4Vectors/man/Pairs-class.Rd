\name{Pairs-class}
\docType{class}

\alias{class:Pairs}
\alias{Pairs-class}

% accessors
\alias{first}
\alias{first,Pairs-method}
\alias{second}
\alias{second,Pairs-method}
\alias{first<-}
\alias{first<-,Pairs-method}
\alias{second<-}
\alias{second<-,Pairs-method}
\alias{names,Pairs-method}
\alias{names<-,Pairs-method}
\alias{length,Pairs-method}
\alias{parallelSlotNames,Pairs-method}

% comparison
\alias{match,Pairs,Pairs-method}

% constructor
\alias{Pairs}

% coercion
\alias{zipup,Pairs,missing-method}
\alias{coerce,Pairs,DataFrame-method}
\alias{as.data.frame,Pairs-method}

% displaying
\alias{show,Pairs-method}

\title{Pairs objects}

\description{
  \code{Pairs} is a \code{Vector} that stores two parallel vectors (any
  object that can be a column in a \code{\linkS4class{DataFrame}}). It
  provides conveniences for performing binary operations on the vectors,
  as well as for converting between an equivalent list
  representation. Virtually all of the typical R vector operations
  should behave as expected.

  A typical use case is representing the pairing from a
  \code{\link[IRanges]{findOverlaps}} call, for which
  \code{\link[IRanges]{findOverlapPairs}} is a shortcut.
}

\section{Constructor}{
  \describe{
    \item{}{
      \code{Pairs(first, second, ..., names = NULL, hits = NULL)}:
      Constructs a Pairs object by aligning the vectors \code{first} and
      \code{second}. The vectors must have the same length, unless
      \code{hits} is specified. Arguments in \code{\dots} are combined
      as columns in the \code{mcols} of the result. The \code{names}
      argument specifies the names on the result. If \code{hits} is not
      \code{NULL}, it should be a \code{\linkS4class{Hits}} object that
      collates the elements in \code{first} and \code{second} to produce
      the corresponding pairs.
    }
  }
}

\section{Accessors}{
  In the code snippets below, \code{x} is a \code{Pairs} object.
  \describe{
    \item{}{\code{names(x)}, \code{names(x) <- value}:
      get or set the names}
    \item{}{\code{first(x)}, \code{first(x) <- value}:
      get or set the first member of each pair}
    \item{}{\code{second(x)}, \code{second(x) <- value}:
      get or set the second member of each pair}
  }
}

\section{Coercion}{
  \describe{
    \item{}{\code{zipup(x)}: Interleaves the \code{Pairs} object
      \code{x} into a list, where each element is composed of a
      pair. The type of list depends on the type of the elements.
    }
    \item{}{\code{zipdown(x)}: The inverse of \code{zipup()}. Converts
      \code{x}, a list where every element is of length 2, to a
      \code{Pairs} object, by assuming that each element of the list
      represents a pair.
    }
  }
}

\section{Subsetting}{
  In the code snippets below, \code{x} is a \code{Pairs} object.
  \describe{
    \item{}{\code{x[i]}: Subset the Pairs object.}
  }
}

\author{Michael Lawrence}

\seealso{
  \itemize{
    \item \link{Hits-class}, a typical way to define a pairing.

    \item \code{\link[IRanges]{findOverlapPairs}} in the \pkg{IRanges}
          package, which generates an instance of this class based on
          overlaps.

    \item \link[IRanges]{setops-methods} in the \pkg{IRanges} package,
          for set operations on Pairs objects.
  }
}

\examples{
p <- Pairs(1:10, Rle(1L, 10), score=rnorm(10), names=letters[1:10])
identical(first(p), 1:10)
mcols(p)$score
p %in% p[1:5]
as.data.frame(p)
z <- zipup(p)
first(p) <- Rle(1:10)
identical(zipdown(z), p)
}
\keyword{methods}
\keyword{classes}
