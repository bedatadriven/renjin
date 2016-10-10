\name{Vector-setops}

\alias{Vector-setops}

\alias{union,Vector,Vector-method}
\alias{intersect,Vector,Vector-method}
\alias{setdiff,Vector,Vector-method}
\alias{setequal,Vector,Vector-method}

\title{Set operations on vector-like objects}

\description{
  Perform set operations on \link{Vector} objects.
}

\usage{
\S4method{union}{Vector,Vector}(x, y)

\S4method{intersect}{Vector,Vector}(x, y)

\S4method{setdiff}{Vector,Vector}(x, y)

\S4method{setequal}{Vector,Vector}(x, y)
}

\arguments{
  \item{x, y}{
    Vector-like objects.
  }
}

\details{
  The \code{union}, \code{intersect}, and \code{setdiff} methods for
  \link{Vector} objects return a \link{Vector} object containing respectively
  the union, intersection, and (asymmetric!) difference of the 2 sets of
  vector elements in \code{x} and \code{y}.
  The \code{setequal} method for \link{Vector} objects checks for \emph{set
  equality} between \code{x} and \code{y}.

  They're defined as follow:
  \preformatted{    setMethod("union", c("Vector", "Vector"),
        function(x, y) unique(c(x, y))
    )
    setMethod("intersect", c("Vector", "Vector"),
        function(x, y) unique(x[x \%in\% y])
    )
    setMethod("setdiff", c("Vector", "Vector"),
        function(x, y) unique(x[!(x \%in\% y)])
    )
    setMethod("setequal", c("Vector", "Vector"),
        function(x, y) all(x \%in\% y) && all(y \%in\% x)
    )
  }
  so they work out-of-the-box on \link{Vector} objects for which \code{c},
  \code{unique}, and \code{\%in\%} are defined.
}

\value{
  \code{union} returns a \link{Vector} object obtained by appending to \code{x}
  the elements in \code{y} that are not already in \code{x}.

  \code{intersect} returns a \link{Vector} object obtained by keeping only
  the elements in \code{x} that are also in \code{y}.

  \code{setdiff} returns a \link{Vector} object obtained by dropping from
  \code{x} the elements that are in \code{y}.

  \code{setequal} returns \code{TRUE} if \code{x} and \code{y} contain the
  same \emph{sets} of vector elements and \code{FALSE} otherwise.

  \code{union}, \code{intersect}, and \code{setdiff} propagate the names and
  metadata columns of their first argument (\code{x}).
}

\author{Hervé Pagès}

\seealso{
  \itemize{
    \item \link{Vector-comparison} for comparing and ordering vector-like
          objects.

    \item \link{Vector} objects.

    \item \code{BiocGenerics::\link[BiocGenerics]{union}},
          \code{BiocGenerics::\link[BiocGenerics]{intersect}},
          and \code{BiocGenerics::\link[BiocGenerics]{setdiff}}
          in the \pkg{BiocGenerics} package for general information about
          these generic functions.
  }
}

\examples{
## See ?`Hits-setops` for some examples.
}

\keyword{methods}
