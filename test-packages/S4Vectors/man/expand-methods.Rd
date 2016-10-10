\name{expand}

\alias{expand}
\alias{expand,DataFrame-method}
\alias{expand,Vector-method}

\title{Unlist the list-like columns of a DataFrame object}
\description{
  \code{expand} transforms a \link{DataFrame} object into a new
  \link{DataFrame} object where the columns specified by the user are
  unlisted. The transformed \link{DataFrame} object has the same colnames
  as the original but typically more rows.
}

\usage{
\S4method{expand}{DataFrame}(x, colnames, keepEmptyRows = FALSE)
}

\arguments{
  \item{x}{
    A \link{DataFrame} object with list-like columns or a \link{Vector}
    object with list-like metadata columns (i.e. with list-like columns in
    \code{mcols(x)}).
  }
  \item{colnames}{
    A \code{character} or \code{numeric} vector containing the names 
    or indices of the list-like columns to unlist. The order in which
    columns are unlisted is controlled by the column order in this vector.
    This defaults to all of the recursive (list-like) columns in \code{x}.
  }
  \item{keepEmptyRows}{
    A \code{logical} indicating if rows containing empty list elements in the 
    specified \code{colnames} should be retained or dropped. When \code{TRUE},
    list elements are replaced with NA and all rows are kept. When
    \code{FALSE}, rows with empty list elements in the \code{colnames}
    columns are dropped.
  }
}

\value{
  A \link{DataFrame} object that has been expanded row-wise to match the
  length of the unlisted columns.
}

\seealso{
  \itemize{
    \item \link{DataFrame} objects.
  }
}

\examples{
library(IRanges)
aa <- CharacterList("a", paste0("d", 1:2), paste0("b", 1:3), c(), "c")
bb <- CharacterList(paste0("sna", 1:2),"foo", paste0("bar",1:3),c(),"hica")
df <- DataFrame(aa=aa, bb=bb, cc=11:15)

## Expand by all list-like columns (aa, bb), dropping rows with empty
## list elements:
expand(df)

## Expand the aa column only:
expand(df, colnames="aa", keepEmptyRows=TRUE)
expand(df, colnames="aa", keepEmptyRows=FALSE)

## Expand the aa and then the bb column:
expand(df, colnames=c("aa","bb"), keepEmptyRows=TRUE)
expand(df, colnames=c("aa","bb"), keepEmptyRows=FALSE)
}

\keyword{methods}
