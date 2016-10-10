\name{zip-methods}

\alias{zipup}
\alias{zipup,ANY,ANY-method}

\alias{zipdown}
\alias{zipdown,ANY-method}
\alias{zipdown,List-method}

\title{Convert between parallel vectors and lists}
\description{
  The \code{zipup} and \code{zipdown} functions convert between two
  parallel vectors and a list of doublets (elements of length 2). The
  metaphor, borrowed from Python's \code{zip}, is that of a zipper. The
  \code{zipup} function interleaves the elements of the parallel vectors
  into a list of doublets. The inverse operation is \code{zipdown},
  which returns a \code{\linkS4class{Pairs}} object.
}

\usage{
zipup(x, y, ...)
zipdown(x, ...)
}

\arguments{
  \item{x,y}{
    For \code{zipup}, any vector-like object. For \code{zipdown}, a
    doublet list.
  }
  \item{\dots}{
    Arguments passed to methods.
  }
}

\value{
  For \code{zipup}, a list-like object, where every element is of length 2.
  For \code{zipdown}, a \code{\linkS4class{Pairs}} object.
}

\seealso{
  \itemize{
    \item \linkS4class{Pairs} objects.
  }
}

\examples{
z <- zipup(1:10, Rle(1L, 10))
pairs <- zipdown(z)
}

\keyword{methods}
