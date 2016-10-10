\name{Rle-class}
\docType{class}

\alias{class:Rle}
\alias{Rle-class}
\alias{Rle}
\alias{Rle,ANY-method}
\alias{Rle,Rle-method}

\alias{length,Rle-method}
\alias{runLength}
\alias{runLength,Rle-method}
\alias{runValue}
\alias{runValue,Rle-method}
\alias{nrun}
\alias{nrun,Rle-method}
\alias{start,Rle-method}
\alias{end,Rle-method}
\alias{width,Rle-method}

\alias{runLength<-}
\alias{runLength<-,Rle-method}
\alias{runValue<-}
\alias{runValue<-,Rle-method}

\alias{as.vector,Rle-method}
\alias{as.vectorORfactor}
\alias{as.vectorORfactor,Rle-method}
\alias{as.factor,Rle-method}
\alias{as.data.frame,Rle-method}
\alias{as.list,Rle-method}
\alias{coerce,Rle,list-method}
\alias{coerce,vector,Rle-method}
\alias{coerce,logical,Rle-method}
\alias{coerce,integer,Rle-method}
\alias{coerce,numeric,Rle-method}
\alias{coerce,complex,Rle-method}
\alias{coerce,character,Rle-method}
\alias{coerce,raw,Rle-method}
\alias{coerce,factor,Rle-method}
\alias{coerce,Rle,vector-method}
\alias{coerce,Rle,logical-method}
\alias{coerce,Rle,integer-method}
\alias{coerce,Rle,numeric-method}
\alias{coerce,Rle,complex-method}
\alias{coerce,Rle,character-method}
\alias{coerce,Rle,raw-method}
\alias{coerce,Rle,factor-method}
\alias{coerce,Rle,data.frame-method}

\alias{extractROWS,Rle,ANY-method}
\alias{[,Rle-method}
\alias{[<-,Rle-method}
\alias{extractROWS,Rle,RangeNSBS-method}

\alias{rev.Rle}
\alias{rev,Rle-method}
\alias{rep.int,Rle-method}
\alias{rep,Rle-method}

\alias{c,Rle-method}
\alias{append,Rle,vector-method}
\alias{append,vector,Rle-method}
\alias{\%in\%,Rle,ANY-method}
\alias{findRun}
\alias{findRun,Rle-method}
\alias{is.na,Rle-method}
\alias{is.unsorted,Rle-method}
\alias{match,Rle,ANY-method}
\alias{show,Rle-method}
\alias{showAsCell,Rle-method}
\alias{order,Rle-method}
\alias{sort,Rle-method}
\alias{table,Rle-method}
\alias{unique,Rle-method}
\alias{duplicated,Rle-method}

\alias{setdiff,Rle,Rle-method}
\alias{setdiff,ANY,Rle-method}
\alias{setdiff,Rle,ANY-method}
\alias{intersect,Rle,Rle-method}
\alias{intersect,ANY,Rle-method}
\alias{intersect,Rle,ANY-method}
\alias{union,Rle,Rle-method}
\alias{union,ANY,Rle-method}
\alias{union,Rle,ANY-method}

\title{Rle objects}

\description{
  The Rle class is a general container for storing an atomic vector
  that is stored in a run-length encoding format. It is based on the
  \code{\link[base]{rle}} function from the base package.
}

\section{Constructor}{
  \describe{
    \item{}{
      \code{Rle(values, lengths)}:
      This constructor creates an Rle instance out of an atomic
      vector or factor object \code{values} and an integer or numeric vector
      \code{lengths} with all positive elements that represent how many times
      each value is repeated. The length of these two vectors must be the same.
      \code{lengths} can be missing in which case \code{values} is turned
      into an Rle.
    }
  }
}

\section{Getters}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{runLength(x)}:
      Returns the run lengths for \code{x}.
    }
    \item{}{
      \code{runValue(x)}:
      Returns the run values for \code{x}.
    }
    \item{}{
      \code{nrun(x)}:
      Returns the number of runs in \code{x}.
    }
    \item{}{
      \code{start(x)}:
      Returns the starts of the runs for \code{x}.
    }
    \item{}{
      \code{end(x)}:
      Returns the ends of the runs for \code{x}.
    }
    \item{}{
      \code{width(x)}:
      Same as \code{runLength(x)}.
    }
  }
}

\section{Setters}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{runLength(x) <- value}:
      Replaces \code{x} with a new Rle object using run values
      \code{runValue(x)} and run lengths \code{value}.
    }
    \item{}{
      \code{runValue(x) <- value}:
      Replaces \code{x} with a new Rle object using run values
      \code{value} and run lengths \code{runLength(x)}.
    }
  }
}

\section{Coercion}{
  \subsection{From atomic vector to Rle}{
    In the code snippets below, \code{from} is an atomic vector:
    \describe{
      \item{}{
        \code{as(from, "Rle")}:
        This coercion creates an Rle instances out of an atomic
        vector \code{from}.
      }
    }
  }
  \subsection{From Rle to other objects}{
    In the code snippets below, \code{x} and \code{from} are Rle objects:
    \describe{
      \item{}{
        \code{as.vector(x, mode="any")}, \code{as(from, "vector")}:
        Creates an atomic vector based on the values contained in
        \code{x}. The vector will be coerced to the requested \code{mode},
        unless \code{mode} is "any", in which case the most appropriate
        type is chosen.
      }
      \item{}{
        \code{as.vectorORfactor(x)}: Creates an atomic vector or factor,
        based on the type of values contained in \code{x}. This is the
        most general way to decompress the Rle to a native R data
        structure.
      }
      \item{}{
        \code{as.factor(x)}, \code{as(from, "factor")}: Creates a factor object
        based on the values contained in \code{x}.
      }
      \item{}{
        \code{as.data.frame(x)}, \code{as(from, "data.frame")}: Creates
        a \code{data.frame} with a single column holding the result of
        \code{as.vector(x)}.
      }
    }
  }
}

\section{General Methods}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{x[i, drop=getOption("dropRle", default=FALSE)]}:
      Subsets \code{x} by index \code{i}, where \code{i} can be positive
      integers, negative integers, a logical vector of the same length as
      \code{x}, an Rle object of the same length as \code{x}
      containing logical values, or an \link[IRanges]{IRanges} object.
      When \code{drop=FALSE} returns an Rle object. When \code{drop=TRUE},
      returns an atomic vector.
    }
    \item{}{
      \code{x[i] <- value}:
      Replaces elements in \code{x} specified by \code{i} with corresponding
      elements in \code{value}. Supports the same types for \code{i} as
      \code{x[i]}.
    }
    \item{}{
      \code{x \%in\% table}:
      Returns a logical Rle representing set membership in
      \code{table}.
    }
    \item{}{
      \code{append(x, values, after = length(x))}:
      Insert one Rle into another Rle.
      \describe{
        \item{\code{values}}{the Rle to insert.}
        \item{\code{after}}{the subscript in \code{x} after which the values
          are to be inserted.}
      }
    }
    \item{}{
      \code{c(x, ...)}:
      Combines a set of Rle objects.
    }
    \item{}{
      \code{findRun(x, vec)}:
      Returns an integer vector indicating the run indices in Rle \code{vec}
      that are referenced by the indices in the integer vector \code{x}.
    }
    \item{}{
      \code{head(x, n = 6L)}:
      If \code{n} is non-negative, returns the first n elements of \code{x}.
      If \code{n} is negative, returns all but the last \code{abs(n)} elements
      of \code{x}.
    }
    \item{}{
      \code{is.na(x)}:
      Returns a logical Rle indicating with values are \code{NA}.
    }
    \item{}{
      \code{is.unsorted(x, na.rm = FALSE, strictly = FALSE)}:
      Returns a logical value specifying if \code{x} is unsorted.
      \describe{
        \item{\code{na.rm}}{remove missing values from check.}
        \item{\code{strictly}}{check for _strictly_ increasing values.}
      }
    }
    \item{}{
      \code{length(x)}:
      Returns the underlying vector length of \code{x}.
    }
    \item{}{
      \code{match(x, table, nomatch = NA_integer_, incomparables = NULL)}:
      Matches the values in \code{x} to \code{table}:
      \describe{
        \item{\code{table}}{the values to be matched against.}
        \item{\code{nomatch}}{the value to be returned in the case when no
          match is found.}
        \item{\code{incomparables}}{a vector of values that cannot be matched.
          Any value in \code{x} matching a value in this vector is assigned
          the \code{nomatch} value.}
      }
    }
    \item{}{
      \code{rep(x, times, length.out, each)}, \code{rep.int(x, times)}:
      Repeats the values in \code{x} through one of the following conventions:
      \describe{
        \item{\code{times}}{Vector giving the number of times to repeat each
          element if of length \code{length(x)}, or to repeat the whole vector
          if of length 1.}
        \item{\code{length.out}}{Non-negative integer. The desired length of
          the output vector.}
        \item{\code{each}}{Non-negative integer.  Each element of \code{x} is
          repeated \code{each} times.}
      }
    }
    \item{}{
      \code{rev(x)}:
      Reverses the order of the values in \code{x}.
    }
    \item{}{
      \code{show(object)}:
      Prints out the Rle object in a user-friendly way.
    }
    \item{}{
      \code{order(..., na.last = TRUE, decreasing = FALSE, method = c("shell", "radix"))}:
      Returns a permutation which rearranges its first argument
      into ascending or descending order, breaking ties by further
      arguments. See \code{\link[BiocGenerics]{order}}.
    }
    \item{}{
      \code{sort(x, decreasing = FALSE, na.last = NA)}:
      Sorts the values in \code{x}.
      \describe{
        \item{\code{decreasing}}{If \code{TRUE}, sort values in decreasing
          order. If \code{FALSE}, sort values in increasing order.}
        \item{\code{na.last}}{If \code{TRUE}, missing values are placed last.
          If \code{FALSE}, they are placed first. If \code{NA}, they are
          removed.}
      }
    }
    \item{}{
      \code{subset(x, subset)}:
      Returns a new Rle object made of the subset using logical vector
      \code{subset}.
    }
    \item{}{
      \code{table(...)}:
      Returns a table containing the counts of the unique values. Supported
      arguments include \code{useNA} with values of `no' and `ifany'.
      Multiple Rle's must be combined with \code{c()} before calling \code{table}.
    }
    \item{}{
      \code{tail(x, n = 6L)}:
      If \code{n} is non-negative, returns the last n elements of \code{x}.
      If \code{n} is negative, returns all but the first \code{abs(n)} elements
      of \code{x}.
    }
    \item{}{
      \code{unique(x, incomparables = FALSE, ...)}:
      Returns the unique run values. The \code{incomparables} argument takes a
      vector of values that cannot be compared with \code{FALSE} being a special
      value that means that all values can be compared. 
    }
  }
}

\section{Set Operations}{
  In the code snippets below, \code{x} and \code{y} are Rle object or
  some other vector-like object:
  \describe{
    \item{}{
      \code{setdiff(x, y)}: Returns the unique elements in
      \code{x} that are not in \code{y}.
    }
    \item{}{
      \code{union(x, y)}:
      Returns the unique elements in either \code{x} or \code{y}.
    }
    \item{}{
      \code{intersect(x, y)}:
      Returns the unique elements in both \code{x} and \code{y}.
    }
  }
}

\author{P. Aboyoun}

\seealso{
  \link{Rle-utils}, \link{Rle-runstat}, and \link[S4Vectors]{aggregate}
  for more operations on Rle objects.

  \code{\link[base]{rle}}

  \link{Vector-class}
}

\examples{
  x <- Rle(10:1, 1:10)
  x

  runLength(x)
  runValue(x)
  nrun(x)

  diff(x)
  unique(x)
  sort(x)
  x[c(1,3,5,7,9)]
  x > 4

  x2 <- Rle(LETTERS[c(21:26, 25:26)], 8:1)
  table(x2)

  y <- Rle(c(TRUE,TRUE,FALSE,FALSE,TRUE,FALSE,TRUE,TRUE,TRUE))
  y
  as.vector(y)
  rep(y, 10)
  c(y, x > 5)
}

\keyword{methods}
\keyword{classes}
