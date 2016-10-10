\name{Hits-setops}

\alias{Hits-setops}

\alias{union,Hits,Hits-method}

\title{Set operations on Hits objects}

\description{
  Perform set operations on \link{Hits} objects.
}

\details{
  \code{union(x, y)}, \code{intersect(x, y)}, \code{setdiff(x, y)}, and
  \code{setequal(x, y)} work on \link{Hits} objects \code{x} and \code{y}
  only if the objects are \emph{compatible Hits objects}, that is, if they
  have the same subject and query lengths. These operations return respectively
  the union, intersection, (asymmetric!) difference, and equality of the
  \emph{sets} of hits in \code{x} and \code{y}.
}

\value{
  \code{union} returns a \link{Hits} object obtained by appending to \code{x}
  the hits in \code{y} that are not already in \code{x}.

  \code{intersect} returns a \link{Hits} object obtained by keeping only
  the hits in \code{x} that are also in \code{y}.

  \code{setdiff} returns a \link{Hits} object obtained by dropping from
  \code{x} the hits that are in \code{y}.

  \code{setequal} returns \code{TRUE} if \code{x} and \code{y} contain the
  same \emph{sets} of hits and \code{FALSE} otherwise.

  \code{union}, \code{intersect}, and \code{setdiff} propagate the names and
  metadata columns of their first argument (\code{x}).
}

\author{Hervé Pagès and Michael Lawrence}

\seealso{
  \itemize{
    \item \link{Hits} objects.

    \item \link{Hits-comparison} for comparing and ordering hits.

    \item \code{BiocGenerics::\link[BiocGenerics]{union}},
          \code{BiocGenerics::\link[BiocGenerics]{intersect}},
          and \code{BiocGenerics::\link[BiocGenerics]{setdiff}}
          in the \pkg{BiocGenerics} package for general information about
          these generic functions.
  }
}

\examples{
x <- Hits(c(2, 4, 4, 4, 5, 5), c(3, 1, 3, 2, 3, 2), 6, 3,
          score=11:16)
x

y <- Hits(c(1, 3, 4, 4, 5, 5, 5), c(3, 3, 2, 1, 2, 1, 3), 6, 3,
          score=21:27)
y

union(x, y)
union(y, x)  # same hits as in union(x, y), but in different order

intersect(x, y)
intersect(y, x)  # same hits as in intersect(x, y), but in
                 # different order

setdiff(x, y)
setdiff(y, x)

setequal(x, y)
}

\keyword{methods}
