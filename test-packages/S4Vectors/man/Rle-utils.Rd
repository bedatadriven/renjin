\name{Rle-utils}

\alias{Rle-utils}

\alias{Ops,Rle,Rle-method}
\alias{Ops,Rle,vector-method}
\alias{Ops,vector,Rle-method}
\alias{Math,Rle-method}
\alias{Math2,Rle-method}
\alias{Summary,Rle-method}
\alias{Complex,Rle-method}

\alias{summary.Rle}
\alias{summary,Rle-method}

\alias{!,Rle-method}
\alias{which,Rle-method}

\alias{pmax,Rle-method}
\alias{pmin,Rle-method}
\alias{pmax.int,Rle-method}
\alias{pmin.int,Rle-method}
\alias{which.max,Rle-method}
\alias{diff.Rle}
\alias{diff,Rle-method}
\alias{mean.Rle}
\alias{mean,Rle-method}
\alias{var,Rle,missing-method}
\alias{var,Rle,Rle-method}
\alias{cov,Rle,Rle-method}
\alias{cor,Rle,Rle-method}
\alias{sd,Rle-method}
\alias{median.Rle}
\alias{median,Rle-method}
\alias{quantile.Rle}
\alias{quantile,Rle-method}
\alias{mad.Rle}
\alias{mad,Rle-method}
\alias{IQR,Rle-method}

\alias{nchar,Rle-method}
\alias{substr,Rle-method}
\alias{substring,Rle-method}
\alias{chartr,ANY,ANY,Rle-method}
\alias{tolower,Rle-method}
\alias{toupper,Rle-method}
\alias{sub,ANY,ANY,Rle-method}
\alias{gsub,ANY,ANY,Rle-method}
\alias{paste,Rle-method}

\alias{levels.Rle}
\alias{levels,Rle-method}
\alias{levels<-,Rle-method}

% old stuff (Deprecated or Defunct)
\alias{ifelse,ANY,ANY,Rle-method}
\alias{ifelse,ANY,Rle,ANY-method}
\alias{ifelse,ANY,Rle,Rle-method}

\title{Common operations on Rle objects}

\description{
  Common operations on \link{Rle} objects.
}

\section{Group Generics}{
  Rle objects have support for S4 group generic functionality:
  \describe{
    \item{\code{Arith}}{\code{"+"}, \code{"-"}, \code{"*"}, \code{"^"},
      \code{"\%\%"}, \code{"\%/\%"}, \code{"/"}}
    \item{\code{Compare}}{\code{"=="}, \code{">"}, \code{"<"}, \code{"!="}, 
      \code{"<="}, \code{">="}}
    \item{\code{Logic}}{\code{"&"}, \code{"|"}}
    \item{\code{Ops}}{\code{"Arith"}, \code{"Compare"}, \code{"Logic"}}
    \item{\code{Math}}{\code{"abs"}, \code{"sign"}, \code{"sqrt"},
      \code{"ceiling"}, \code{"floor"}, \code{"trunc"}, \code{"cummax"},
      \code{"cummin"}, \code{"cumprod"}, \code{"cumsum"}, \code{"log"},
      \code{"log10"}, \code{"log2"}, \code{"log1p"}, \code{"acos"},
      \code{"acosh"}, \code{"asin"}, \code{"asinh"}, \code{"atan"},
      \code{"atanh"}, \code{"exp"}, \code{"expm1"}, \code{"cos"},
      \code{"cosh"}, \code{"sin"}, \code{"sinh"}, \code{"tan"}, \code{"tanh"},
      \code{"gamma"}, \code{"lgamma"}, \code{"digamma"}, \code{"trigamma"}}
    \item{\code{Math2}}{\code{"round"}, \code{"signif"}}
    \item{\code{Summary}}{\code{"max"}, \code{"min"}, \code{"range"},
      \code{"prod"}, \code{"sum"}, \code{"any"}, \code{"all"}}
    \item{\code{Complex}}{\code{"Arg"}, \code{"Conj"}, \code{"Im"},
      \code{"Mod"}, \code{"Re"}}
  }
  See \link[methods]{S4groupGeneric} for more details.
}

\section{Summary}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{summary(object, ..., digits = max(3, getOption("digits") - 3))}:
      Summarizes the Rle object using an atomic vector convention. The
      \code{digits} argument is used for number formatting with
      \code{signif()}.
    }
  }
}

\section{Logical Data Methods}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{!x}:
       Returns logical negation (NOT) of \code{x}.
    }
    \item{}{
      \code{which(x)}:
       Returns an integer vector representing the \code{TRUE} indices of
       \code{x}.
     }
  }
}

\section{Numerical Data Methods}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{diff(x, lag = 1, differences = 1}:
      Returns suitably lagged and iterated differences of \code{x}.
      \describe{
         \item{\code{lag}}{An integer indicating which lag to use.}
         \item{\code{differences}}{An integer indicating the order of the
           difference.}
      }
    }
    \item{}{
       \code{pmax(..., na.rm = FALSE)}, \code{pmax.int(..., na.rm = FALSE)}:
       Parallel maxima of the Rle input values. Removes \code{NA}s when
       \code{na.rm = TRUE}.
    }
    \item{}{
       \code{pmin(..., na.rm = FALSE)}, \code{pmin.int(..., na.rm = FALSE)}:
       Parallel minima of the Rle input values. Removes \code{NA}s when
       \code{na.rm = TRUE}.
    }
    \item{}{
      \code{which.max(x)}: Returns the index of the first element matching
      the maximum value of \code{x}.
    }
    \item{}{
      \code{mean(x, na.rm = FALSE)}:
      Calculates the mean of \code{x}. Removes \code{NA}s when
      \code{na.rm = TRUE}.
    }
    \item{}{
      \code{var(x, y = NULL, na.rm = FALSE)}:
      Calculates the variance of \code{x} or covariance of \code{x} and \code{y}
      if both are supplied. Removes \code{NA}s when \code{na.rm = TRUE}.
    }
    \item{}{
      \code{cov(x, y, use = "everything")}, \code{cor(x, y, use = "everything")}:
      Calculates the covariance and correlation respectively of Rle objects
      \code{x} and \code{y}.
      The \code{use} argument is an optional character string giving a method for
      computing covariances in the presence of missing values. This must be
      (an abbreviation of) one of the strings \code{"everything"},
      \code{"all.obs"}, \code{"complete.obs"}, \code{"na.or.complete"}, or
      \code{"pairwise.complete.obs"}.
    }
    \item{}{
      \code{sd(x, na.rm = FALSE)}:
      Calculates the standard deviation of \code{x}. Removes \code{NA}s
      when \code{na.rm = TRUE}.
    }
    \item{}{
      \code{median(x, na.rm = FALSE)}:
      Calculates the median of \code{x}. Removes \code{NA}s when
      \code{na.rm = TRUE}.
    }
    \item{}{
      \code{quantile(x, probs = seq(0, 1, 0.25), na.rm = FALSE, names = TRUE, type = 7, ...)}:
      Calculates the specified quantiles of \code{x}.
      \describe{
        \item{\code{probs}}{A numeric vector of probabilities with values in
          [0,1].}
        \item{\code{na.rm}}{If \code{TRUE}, removes \code{NA}s from \code{x}
          before the quantiles are computed.}
        \item{\code{names}}{If \code{TRUE}, the result has names describing the
          quantiles.}
        \item{\code{type}}{An integer between 1 and 9 selecting one of the nine
          quantile algorithms detailed in \code{\link[stats]{quantile}}.}
        \item{\dots}{Further arguments passed to or from other methods.}
      }
    }
    \item{}{
      \code{mad(x, center = median(x), constant = 1.4826, na.rm = FALSE, low = FALSE, high = FALSE)}:
      Calculates the median absolute deviation of \code{x}.
      \describe{
        \item{\code{center}}{The center to calculate the deviation from.}
        \item{\code{constant}}{The scale factor.}
        \item{\code{na.rm}}{If \code{TRUE}, removes \code{NA}s from \code{x}
          before the mad is computed.}
        \item{\code{low}}{If \code{TRUE}, compute the 'lo-median'.}
        \item{\code{high}}{If \code{TRUE}, compute the 'hi-median'.}
      }
    }
    \item{}{
      \code{IQR(x, na.rm = FALSE)}:
      Calculates the interquartile range of \code{x}.
      \describe{
        \item{\code{na.rm}}{If \code{TRUE}, removes \code{NA}s from \code{x}
          before the IQR is computed.}
      }
    }
    \item{}{
      \code{smoothEnds(y, k = 3)}:
      Smooth end points of an Rle \code{y} using subsequently smaller
      medians and Tukey's end point rule at the very end.
      \describe{
        \item{\code{k}}{An integer indicating the width of largest median
          window; must be odd.}
      }
    }
  }
}

\section{Character Data Methods}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{nchar(x, type = "chars", allowNA = FALSE)}:
      Returns an integer Rle representing the number of characters in the
      corresponding values of \code{x}.
      \describe{
         \item{\code{type}}{One of \code{c("bytes", "chars", "width")}.}
         \item{\code{allowNA}}{Should \code{NA} be returned for invalid multibyte
           strings rather than throwing an error?}
      }
    }
    \item{}{
      \code{substr(x, start, stop)}, \code{substring(text, first, last = 1000000L)}:
      Returns a character or factor Rle containing the specified substrings
      beginning at \code{start}/\code{first} and ending at
      \code{stop}/\code{last}.
    }
    \item{}{
      \code{chartr(old, new, x)}:
      Returns a character or factor Rle containing a translated version of
      \code{x}.
      \describe{
         \item{\code{old}}{A character string specifying the characters to be
           translated.}
         \item{\code{new}}{A character string specifying the translations.}
      }
    }
    \item{}{
      \code{tolower(x)}:
      Returns a character or factor Rle containing a lower case version of
      \code{x}.
    }
    \item{}{
      \code{toupper(x)}:
      Returns a character or factor Rle containing an upper case version of
      \code{x}.
    }
    \item{}{
      \code{sub(pattern, replacement, x, ignore.case = FALSE,
                perl = FALSE, fixed = FALSE, useBytes = FALSE)}:
      Returns a character or factor Rle containing replacements based on
      matches determined by regular expression matching. See \code{\link{sub}}
      for a description of the arguments.
    }
    \item{}{
      \code{gsub(pattern, replacement, x, ignore.case = FALSE,
                 perl = FALSE, fixed = FALSE, useBytes = FALSE)}:
      Returns a character or factor Rle containing replacements based on
      matches determined by regular expression matching. See \code{\link{gsub}}
      for a description of the arguments.
    }
    \item{}{
      \code{paste(..., sep = " ", collapse = NULL)}:
      Returns a character or factor Rle containing a concatenation of
      the values in \code{...}.
    }
  }
}

\section{Factor Data Methods}{
  In the code snippets below, \code{x} is an Rle object:

  \describe{
    \item{}{
      \code{levels(x)}, \code{levels(x) <- value}:
      Gets and sets the factor levels, respectively.
    }
    \item{}{
      \code{nlevels(x)}:
      Returns the number of factor levels.
    }
  }
}

\author{P. Aboyoun}

\seealso{
  \link{Rle} objects

  \link[methods]{S4groupGeneric}
}

\examples{
x <- Rle(10:1, 1:10)
x

sqrt(x)
x^2 + 2 * x + 1
range(x)
sum(x)
mean(x)

z <- c("the", "quick", "red", "fox", "jumps", "over", "the", "lazy", "brown", "dog")
z <- Rle(z, seq_len(length(z)))
chartr("a", "@", z)
toupper(z)
}

\keyword{utilities}
\keyword{methods}
\keyword{arith}
