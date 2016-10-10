\name{DataTable-class}
\docType{class}

\alias{class:DataTable}
\alias{DataTable-class}
\alias{DataTable}

\alias{NROW,DataTable-method}
\alias{NCOL,DataTable-method}
\alias{dim,DataTable-method}
\alias{ROWNAMES}
\alias{ROWNAMES,ANY-method}
\alias{ROWNAMES,DataTable-method}
\alias{dimnames,DataTable-method}
\alias{dimnames<-,DataTable-method}

\alias{head.DataTable}
\alias{tail.DataTable}
\alias{subset,DataTable-method}
\alias{na.omit,DataTable-method}
\alias{na.exclude,DataTable-method}
\alias{is.na,DataTable-method}
\alias{complete.cases,DataTable-method}

\alias{cbind,DataTable-method}
\alias{rbind,DataTable-method}
\alias{merge,DataTable,DataTable-method}
\alias{merge,data.frame,DataTable-method}
\alias{merge,DataTable,data.frame-method}

\alias{by,DataTable-method}

\alias{duplicated,DataTable-method}
\alias{duplicated.DataTable}
\alias{unique,DataTable-method}
\alias{unique.DataTable}

\alias{as.env}
\alias{as.env,NULL-method}
\alias{as.env,DataTable-method}

\alias{show,DataTable-method}

\alias{xtabs,DataTable-method}


\title{DataTable objects}

\description{
  DataTable is an API only (i.e. virtual class with no slots) for accessing
  objects with a rectangular shape like \link{DataFrame} or
  \link[IRanges]{RangedData} objects. It mimics the API for standard
  \link{data.frame} objects.
}

\section{Accessors}{
  In the following code snippets, \code{x} is a \code{DataTable}.
  \describe{
    \item{}{
      \code{nrow(x)}, \code{ncol(x)}:
      Get the number of rows and columns, respectively.
    }
    \item{}{
      \code{NROW(x)}, \code{NCOL(x)}:
      Same as \code{nrow(x)} and \code{ncol(x)}, respectively.
    }
    \item{}{
      \code{dim(x)}:
      Length two integer vector defined as \code{c(nrow(x), ncol(x))}.
    }
    \item{}{
      \code{rownames(x)}, \code{colnames(x)}:
      Get the names of the rows and columns, respectively.
    }
    \item{}{
      \code{dimnames(x)}:
      Length two list of character vectors defined as
      \code{list(rownames(x), colnames(x))}.
    }
  }
}

\section{Subsetting}{
  In the code snippets below, \code{x} is a DataTable object.

  \describe{
    \item{}{
      \code{x[i, j, drop=TRUE]}:
      Return a new DataTable object made of the selected rows and columns. For
      single column selection, the \code{drop} argument specifies whether or not
      to coerce the returned sequence to a standard vector.
    }
    \item{}{
      \code{head(x, n=6L)}:
      If \code{n} is non-negative, returns the first n rows of the DataTable
      object.
      If \code{n} is negative, returns all but the last \code{abs(n)} rows of
      the DataTable object.
    }
    \item{}{
      \code{tail(x, n=6L)}:
      If \code{n} is non-negative, returns the last n rows of the DataTable
      object.
      If \code{n} is negative, returns all but the first \code{abs(n)} rows of
      the DataTable object.
    }
    \item{}{
      \code{subset(x, subset, select, drop=FALSE)}:
      Return a new DataTable object using:
      \describe{
        \item{subset}{logical expression indicating rows to keep, where missing
          values are taken as FALSE.}
        \item{select}{expression indicating columns to keep.}
        \item{drop}{passed on to \code{[} indexing operator.}
      }
    }
    \item{}{
      \code{\link[stats:na.fail]{na.omit}(object)}:
      Returns a subset with incomplete cases removed.
    }
    \item{}{
      \code{\link[stats:na.fail]{na.exclude}(object)}:
      Returns a subset with incomplete cases removed (but to be
      included with NAs in statistical results).
    }
    \item{}{
      \code{\link[base:NA]{is.na}(x)}:
      Returns a logical matrix indicating which cells are missing.
    }
    \item{}{
      \code{\link[stats]{complete.cases}(x)}:
      Returns a logical vector identifying which cases have no missing values.
    }
  }
}

\section{Combining}{
  In the code snippets below, \code{x} is a DataTable object.

  \describe{
    \item{}{
      \code{cbind(...)}: Creates a new \code{DataTable} by combining the columns
      of the \code{DataTable} objects in \code{...}.
    }
    \item{}{
      \code{rbind(...)}: Creates a new \code{DataTable} by combining the rows of
      the \code{DataTable} objects in \code{...}.
    }
    \item{}{
      \code{merge(x, y, ...)}: Merges two \code{DataTable}
      objects \code{x} and \code{y}, with arguments in \code{...} being
      the same as those allowed by the base \code{\link{merge}}. It is
      allowed for either \code{x} or \code{y} to be a \code{data.frame}.
    }
  }
}

\section{Looping}{
  In the code snippets below, \code{x} is a DataTable object.

  \describe{
    \item{}{
      \code{by(data, INDICES, FUN, ..., simplify = TRUE)}:
      Apply \code{FUN} to each group of \code{data}, a \code{DataTable},
      formed by the factor (or list of factors) \code{INDICES}. Exactly 
      the same contract as \code{\link{as.data.frame}}.
    }
  }
}

\section{Utilities}{
  \describe{
    \item{}{\code{duplicated(x)}: Returns a logical vector indicating the rows
      that are identical to a previous row.
    }
    \item{}{\code{unique(x)}: Returns a new \code{DataTable} after
      removing the duplicated rows from \code{x}.
    }
    \item{}{
      \code{show(x)}:
      By default the \code{show} method displays 5 head and 5 tail
      lines. The number of lines can be altered by setting the global
      options \code{showHeadLines} and \code{showTailLines}. If the 
      object length is less than the sum of the options, the full object 
      is displayed. These options affect GRanges, GAlignments, 
      Ranges, DataTable and XString objects.
    }
  }
}

\section{Coercion}{
  \describe{
    \item{}{\code{as.env(x, enclos = parent.frame())}:
      Creates an environment from \code{x} with a symbol for each
      \code{colnames(x)}. The values are not actually copied into the
      environment. Rather, they are dynamically bound using
      \code{\link{makeActiveBinding}}. This prevents unnecessary copying
      of the data from the external vectors into R vectors. The values
      are cached, so that the data is not copied every time the symbol
      is accessed.
    }
  }
}

\section{Statistical modeling with DataTable}{
  A number of wrappers are implemented for performing statistical
  procedures, such as model fitting, with DataTable objects.
  \subsection{Tabulation}{
    \describe{
      \item{}{\code{xtabs(formula = ~., data, subset, na.action,
          exclude = c(NA, NaN), drop.unused.levels = FALSE)}:
        Like the original \code{\link{xtabs}}, except \code{data} is a
        \code{DataTable}.
      }
    }
  }
}

\seealso{
  \itemize{
    \item \link{DataFrame} for an implementation that mimics \code{data.frame}.

    \item \link{data.frame}
  }
}

\examples{
showClass("DataTable")  # shows (some of) the known subclasses

library(IRanges)
df <- DataFrame(as.data.frame(UCBAdmissions))
xtabs(Freq ~ Gender + Admit, df)
}

\keyword{methods}
\keyword{classes}
