\name{FilterMatrix-class}
\docType{class}
\alias{FilterMatrix-class}

% accessors
\alias{filterRules,FilterMatrix-method}

% subsetting
\alias{[,FilterMatrix-method}

% splitting and combining
\alias{cbind,FilterMatrix-method}
\alias{rbind,FilterMatrix-method}

% constructor
\alias{FilterMatrix}

% utilities
\alias{show,FilterMatrix-method}
\alias{summary,FilterMatrix-method}

\title{Matrix for Filter Results}

\description{A \code{FilterMatrix} object is a matrix meant for storing
  the logical output of a set of \code{\linkS4class{FilterRules}}, where
  each rule corresponds to a column. The \code{FilterRules} are stored
  within the \code{FilterMatrix} object, for the sake of
  provenance. In general, a \code{FilterMatrix} behaves like an
  ordinary \code{\link{matrix}}.
}

\section{Accessor methods}{
  In the code snippets below, \code{x} is a \code{FilterMatrix} object.

  \describe{
    \item{}{\code{filterRules(x)}: Get the \code{FilterRules}
      corresponding to the columns of the matrix.
    }
  }
}

\section{Constructor}{
  \describe{
    \item{}{
      \code{FilterMatrix(matrix, filterRules)}: Constructs a
      \code{FilterMatrix}, from a given \code{matrix} and
      \code{filterRules}. Not usually called by the user, see
      \code{\link{evalSeparately}}.
    }
  }
}

\section{Utilities}{
  \describe{
    \item{}{
      \code{summary(object, discarded = FALSE, percent = FALSE)}:
      Returns a numeric vector containing the total number of records
      (\code{nrow}), the number passed by each filter, and the number of
      records that passed every filter. If \code{discarded} is
      \code{TRUE}, then the numbers are inverted (i.e., the values are
      subtracted from the number of rows). If \code{percent} is
      \code{TRUE}, then the numbers are percent of total.
    }
  }
}

\author{ Michael Lawrence }
\seealso{
  \itemize{
    \item \code{\link{evalSeparately}} is the typical way to generate this
          object.

    \item \link{FilterRules} objects.
  }
}

\keyword{classes}
\keyword{methods}
