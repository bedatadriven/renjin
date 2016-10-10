\name{SimpleList-class}
\docType{class}

\alias{class:SimpleList}
\alias{SimpleList}
\alias{SimpleList-class}

% accessors
\alias{length,SimpleList-method}
\alias{names,SimpleList-method}
\alias{names<-,SimpleList-method}

% coercion
\alias{as.list,SimpleList-method}
\alias{coerce,ANY,SimpleList-method}
\alias{coerce,list,List-method}

% combining
\alias{c,SimpleList-method}

% looping
\alias{lapply,SimpleList-method}
\alias{endoapply,SimpleList-method}
\alias{mendoapply,SimpleList-method}

% displaying
\alias{classNameForDisplay,SimpleList-method}


\title{SimpleList objects}

\description{
  The (non-virtual) SimpleList class extends the \link{List}
  virtual class.
}

\details{
  The SimpleList class is the simplest, most generic concrete implementation
  of the \link{List} abstraction. It provides an implementation that
  subclasses can easily extend.

  In a SimpleList object the list elements are stored internally in an
  ordinary list.
}

\section{Constructor}{
  See the \link{List} man page for a quick overview of how to construct
  \link{List} objects in general.

  The following constructor is provided for SimpleList objects:
  \describe{
    \item{}{\code{SimpleList(...)}: Takes possibly named objects as elements
      for the new SimpleList object.
    }
  }
}

\section{Accessors}{
  Same as for \link{List} objects. See the \link{List} man page for more
  information.
}

\section{Coercion}{
  All the coercions documented in the \link{List} man page apply to
  \link{SimpleList} objects.
}

\section{Subsetting}{
  Same as for \link{List} objects. See the \link{List} man page for more
  information.
}

\section{Looping and functional programming}{
  Same as for \link{List} objects. See \code{?`\link{List-utils}`} for
  more information.
}

\section{Displaying}{
  When a SimpleList object is displayed, the "Simple" prefix is removed
  from the real class name of the object.
  See \code{\link{classNameForDisplay}} for more information about this.
}

\seealso{
  \itemize{
    \item \link{List} objects for the parent class.

    \item The \link[IRanges]{CompressedList} class defined in the
          \pkg{IRanges} package for a more efficient alternative
          to SimpleList.

    \item The \link[IRanges]{SimpleIntegerList} class defined in the
          \pkg{IRanges} package for a SimpleList subclass example.

    \item The \link{DataFrame} class for another SimpleList subclass example.
  }
}

\examples{
## Displaying a SimpleList object:
x1 <- SimpleList(a=letters, i=Rle(22:20, 4:2))
class(x1)

## The "Simple" prefix is removed from the real class name of the
## object:
x1

library(IRanges)
x2 <- IntegerList(11:12, integer(0), 3:-2, compress=FALSE)
class(x2)

## The "Simple" prefix is removed from the real class name of the
## object:
x2

## This is controlled by internal helper classNameForDisplay():
classNameForDisplay(x2)
}
\keyword{methods}
\keyword{classes}
