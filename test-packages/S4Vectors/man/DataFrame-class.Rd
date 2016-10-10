\name{DataFrame-class}
\docType{class}

\alias{class:DataFrame}
\alias{DataFrame-class}

% accessor
\alias{nrow,DataFrame-method}
\alias{ncol,DataFrame-method}
\alias{rownames,DataFrame-method}
\alias{colnames,DataFrame-method}
\alias{rownames<-,DataFrame-method}
\alias{colnames<-,DataFrame-method}

% constructor
\alias{DataFrame}

% subsetting
\alias{[,DataFrame-method}
\alias{[<-,DataFrame-method}
\alias{[[<-,DataFrame-method}

% coercion
\alias{as.data.frame,DataFrame-method}
\alias{as.matrix,DataFrame-method}
\alias{coerce,matrix,DataFrame-method}
\alias{coerce,vector,DataFrame-method}
\alias{coerce,list,DataFrame-method}
\alias{coerce,integer,DataFrame-method}
\alias{coerce,Vector,DataFrame-method}
\alias{coerce,data.frame,DataFrame-method}
\alias{coerce,NULL,DataFrame-method}
\alias{coerce,table,DataFrame-method}
\alias{coerce,AsIs,DataFrame-method}
\alias{coerce,DataFrame,data.frame-method}
\alias{coerce,xtabs,DataFrame-method}
\alias{coerce,ANY,DataFrame-method}
\alias{coerce,SimpleList,DataFrame-method}
\alias{coerce,ANY,AsIs-method}

% combining
\alias{cbind,DataFrame-method}
\alias{rbind,DataFrame-method}

\title{DataFrame objects}

\description{
  The \code{DataFrame} class extends the \link{DataTable} virtual
  class and supports the storage of any type of object (with \code{length}
  and \code{[} methods) as columns.
}

\details{
  On the whole, the \code{DataFrame} behaves very similarly to
  \code{data.frame}, in terms of construction, subsetting, splitting,
  combining, etc. The most notable exception is that the row names are
  optional. This means calling \code{rownames(x)} will return
  \code{NULL} if there are no row names. Of course, it could return
  \code{seq_len(nrow(x))}, but returning \code{NULL} informs, for
  example, combination functions that no row names are desired (they are
  often a luxury when dealing with large data).

  As \code{DataFrame} derives from \code{\linkS4class{Vector}}, it is
  possible to set an \code{annotation} string. Also, another
  \code{DataFrame} can hold metadata on the columns.

  For a class to be supported as a column, it must have \code{length}
  and \code{[} methods, where \code{[} supports subsetting only by
  \code{i} and respects \code{drop=FALSE}. Optionally, a method may be
  defined for the \code{showAsCell} generic, which should return a
  vector of the same length as the subset of the column passed to
  it. This vector is then placed into a \code{data.frame} and converted
  to text with \code{format}. Thus, each element of the vector should be
  some simple, usually character, representation of the corresponding
  element in the column.
}

\section{Constructor}{
  \describe{\code{DataFrame(..., row.names = NULL, check.names = TRUE)}:
    Constructs a \code{DataFrame} in similar fashion to
    \code{\link{data.frame}}. Each argument in \code{...} is coerced to
    a \code{DataFrame} and combined column-wise. No special effort is
    expended to automatically determine the row names from the
    arguments. The row names should be given in
    \code{row.names}; otherwise, there are no row names. This is by
    design, as row names are normally undesirable when data is
    large. If \code{check.names} is \code{TRUE}, the column names will
    be checked for syntactic validity and made unique, if necessary.

    To store an object of a class that does not support coercion to
    \code{DataFrame}, wrap it in \code{I()}. The class must still have
    methods for \code{length} and \code{[}.
  }
}

\section{Accessors}{
  In the following code snippets, \code{x} is a \code{DataFrame}.
  \describe{
    \item{}{\code{dim(x)}:
      Get the length two integer vector indicating in the first and
      second element the number of rows and columns, respectively.
    }
    \item{}{\code{dimnames(x)}, \code{dimnames(x) <- value}:
      Get and set the two element list containing the row names
      (character vector of length \code{nrow(x)} or \code{NULL})
      and the column names (character vector of length \code{ncol(x)}).
    }
  }
}

\section{Coercion}{
  \describe{
    \item{}{\code{as(from, "DataFrame")}:
      By default, constructs a new \code{DataFrame} with \code{from} as
      its only column. If \code{from} is a \code{matrix} or
      \code{data.frame}, all of its columns become columns in the new
      \code{DataFrame}. If \code{from} is a list, each element becomes a
      column, recycling as necessary. Note that for the \code{DataFrame}
      to behave correctly, each column object must support element-wise
      subsetting via the \code{[} method and return the number of elements with
      \code{length}. It is recommended to use the \code{DataFrame} 
      constructor, rather than this interface.
    }
    \item{}{\code{as.list(x)}: Coerces \code{x}, a \code{DataFrame},
      to a \code{list}.
    }
    \item{}{\code{as.data.frame(x, row.names=NULL, optional=FALSE)}:
      Coerces \code{x}, a \code{DataFrame}, to a \code{data.frame}.
      Each column is coerced to a \code{data.frame} and then column
      bound together. If \code{row.names} is \code{NULL}, they are
      retrieved from \code{x}, if it has any. Otherwise, they are
      inferred by the \code{data.frame} constructor.

      NOTE: conversion of \code{x} to a \code{data.frame} is not
      supported if \code{x} contains any \code{list}, \code{SimpleList},
      or \code{CompressedList} columns.
    }
    \item{}{\code{as(from, "data.frame")}: Coerces a \code{DataFrame}
      to a \code{data.frame} by calling \code{as.data.frame(from)}.
    }
    \item{}{\code{as.matrix(x)}: Coerces the \code{DataFrame} to a
      \code{matrix}, if possible.
    }
  }
}

\section{Subsetting}{
  In the following code snippets, \code{x} is a \code{DataFrame}.
  \describe{
    \item{}{\code{x[i,j,drop]}: Behaves very similarly to the
      \code{\link{[.data.frame}} method, except \code{i} can be a
      logical \code{Rle} object and subsetting by \code{matrix} indices
      is not supported. Indices containing \code{NA}'s are also not
      supported.
    }
    \item{}{\code{x[i,j] <- value}: Behaves very similarly to the
      \code{\link{[<-.data.frame}} method.
    }
    \item{}{\code{x[[i]]}: Behaves very similarly to the
      \code{\link{[[.data.frame}} method, except arguments \code{j}
      and \code{exact} are not supported. Column name matching is
      always exact. Subsetting by matrices is not supported.
    }
    \item{}{\code{x[[i]] <- value}: Behaves very similarly to the
      \code{\link{[[<-.data.frame}} method, except argument \code{j}
      is not supported.
    }
  }
}

\section{Combining}{
  In the following code snippets, \code{x} is a \code{DataFrame}.

  \describe{
    \item{}{
      \code{rbind(...)}: Creates a new \code{DataFrame} by
      combining the rows of the \code{DataFrame} objects in
      \code{...}. Very similar to \code{\link{rbind.data.frame}}, except
      in the handling of row names. If all elements have row names, they
      are concatenated and made unique. Otherwise, the result does not
      have row names. Currently, factors are not handled well (their
      levels are dropped). This is not a high priority until there is an
      \code{XFactor} class.
    }
    \item{}{
      \code{cbind(...)}: Creates a new \code{DataFrame} by
      combining the columns of the \code{DataFrame} objects in
      \code{...}. Very similar to \code{\link{cbind.data.frame}}, except
      row names, if any, are dropped. Consider the \code{DataFrame}
      as an alternative that allows one to specify row names.
    }
  }
}

\author{Michael Lawrence}

\seealso{
  \itemize{
    \item \link{DataTable} and \link{SimpleList} which DataFrame extends
          directly.
  }
}

\examples{
score <- c(1L, 3L, NA)
counts <- c(10L, 2L, NA)
row.names <- c("one", "two", "three")
  
df <- DataFrame(score) # single column
df[["score"]]
df <- DataFrame(score, row.names = row.names) #with row names
rownames(df)
  
df <- DataFrame(vals = score) # explicit naming
df[["vals"]]

# arrays
ary <- array(1:4, c(2,1,2))
sw <- DataFrame(I(ary))  
  
# a data.frame
sw <- DataFrame(swiss)
as.data.frame(sw) # swiss, without row names
# now with row names
sw <- DataFrame(swiss, row.names = rownames(swiss))
as.data.frame(sw) # swiss

# subsetting
    
sw[] # identity subset
sw[,] # same

sw[NULL] # no columns
sw[,NULL] # no columns
sw[NULL,] # no rows

## select columns
sw[1:3]
sw[,1:3] # same as above
sw[,"Fertility"]
sw[,c(TRUE, FALSE, FALSE, FALSE, FALSE, FALSE)]

## select rows and columns
sw[4:5, 1:3]
  
sw[1] # one-column DataFrame
## the same
sw[, 1, drop = FALSE]
sw[, 1] # a (unnamed) vector
sw[[1]] # the same
sw[["Fertility"]]

sw[["Fert"]] # should return 'NULL'
 
sw[1,] # a one-row DataFrame
sw[1,, drop=TRUE] # a list

## duplicate row, unique row names are created
sw[c(1, 1:2),]

## indexing by row names  
sw["Courtelary",]
subsw <- sw[1:5,1:4]
subsw["C",] # partially matches

## row and column names
cn <- paste("X", seq_len(ncol(swiss)), sep = ".")
colnames(sw) <- cn
colnames(sw)
rn <- seq(nrow(sw))
rownames(sw) <- rn
rownames(sw)

## column replacement

df[["counts"]] <- counts
df[["counts"]]
df[[3]] <- score
df[["X"]]
df[[3]] <- NULL # deletion
}
\keyword{classes}
\keyword{methods}
