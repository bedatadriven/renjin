\name{FilterRules-class}
\docType{class}

\alias{class:expressionORfunction}
\alias{expressionORfunction-class}
\alias{expressionORfunction}

\alias{class:FilterRules}
\alias{FilterRules-class}

% accessors
\alias{active}
\alias{active,FilterRules-method}
\alias{active<-}
\alias{active<-,FilterRules-method}

% subsetting
\alias{[,FilterRules-method}
\alias{[[<-,FilterRules-method}
\alias{subsetByFilter}
\alias{subsetByFilter,ANY,FilterRules-method}

% splitting and combining
\alias{append,FilterRules,FilterRules-method}
\alias{c,FilterRules-method}

% evaluating
\alias{eval,FilterRules,ANY-method}
\alias{evalSeparately}
\alias{evalSeparately,FilterRules-method}

% constructor
\alias{FilterRules}

% general 
\alias{summary,FilterRules-method}

% filter closures
\alias{params}
\alias{params,FilterClosure-method}
\alias{coerce,standardGeneric,FilterClosure-method}
\alias{coerce,function,FilterClosure-method}
\alias{show,FilterClosure-method}

\title{Collection of Filter Rules}
\description{A \code{FilterRules} object is a collection of filter
  rules, which can be either \code{expression} or \code{function}
  objects. Rules can be disabled/enabled individually, facilitating
  experimenting with different combinations of filters.}

\details{
  It is common to split a dataset into subsets during data
  analysis. When data is large, however, representing subsets (e.g. by
  logical vectors) and storing them as copies might become too costly in
  terms of space. The \code{FilterRules} class represents
  subsets as lightweight \code{expression} and/or \code{function}
  objects. Subsets can then be calculated when needed (on the fly). This
  avoids copying and storing a large number of subsets. Although it
  might take longer to frequently recalculate a subset, it often is a
  relatively fast operation and the space savings tend to be more than
  worth it when data is large.

  Rules may be either expressions or functions. Evaluating an expression
  or invoking a function should result in a logical vector. Expressions
  are often more convenient, but functions (i.e. closures) are generally
  safer and more powerful, because the user can specify the enclosing
  environment. If a rule is an expression, it is evaluated inside the
  \code{envir} argument to the \code{eval} method (see below). If a
  function, it is invoked with \code{envir} as its only
  argument. See examples.
}

\section{Accessor methods}{
  In the code snippets below, \code{x} is a \code{FilterRules} object.

  \describe{
    \item{}{\code{active(x)}: Get the logical vector of length
    \code{length(x)}, where \code{TRUE} for an element indicates that
    the corresponding rule in \code{x} is active (and inactive
    otherwise). Note that \code{names(active(x))} is equal to
    \code{names(x)}.}
  
    \item{}{\code{active(x) <- value}: Replace the active state of the
    filter rules. If \code{value} is a logical vector, it should be of
    length \code{length(x)} and indicate which rules are
    active. Otherwise, it can be either numeric or character vector, in which
    case it sets the indicated rules (after dropping NA's) to active and
    all others to inactive. See examples.}
  }
}

\section{Constructor}{
  \describe{
    \item{}{
      \code{FilterRules(exprs = list(), ..., active = TRUE)}:
      Constructs a \code{FilterRules} with the rules given in the list
      \code{exprs} or in \code{...}. The initial active state of the rules
      is given by \code{active}, which is recycled as
      necessary. Elements in \code{exprs} may be either character (parsed
      into an expression), a language object (coerced to an expression), an
      expression, or a function that takes at least one
      argument. \strong{IMPORTANTLY}, all arguments in \code{...} are
      \strong{\code{quote()}}'d and then coerced to an expression. So,
      for example, character data is only parsed if it is a literal.
      The names of the filters are taken from the names of
      \code{exprs} and \code{...}, if given. Otherwise,
      the character vectors take themselves as their name and the
      others are deparsed (before any coercion). Thus, it is recommended
      to always specify meaningful names. In any case, the names
      are made valid and unique.
    }
  }
}

\section{Subsetting and Replacement}{
  In the code snippets below, \code{x} is a \code{FilterRules} object.

  \describe{
    \item{}{
      \code{x[i]}: Subsets the filter rules using the
      same interface as for \code{\linkS4class{Vector}}.
    }
    \item{}{
      \code{x[[i]]}: Extracts an expression or function via the same
      interface as for \code{\linkS4class{List}}.
    }
    \item{}{
      \code{x[[i]] <- value}: The same interface as for
      \code{\linkS4class{List}}. The default active state for new
      rules is \code{TRUE}.
    }
  }
}

\section{Combining}{
  In the code snippets below, \code{x} is a \code{FilterRules} object.
  
  \describe{
    \item{}{\code{append(x, values, after = length(x))}: Appends the
      \code{values} \code{FilterRules} instance onto \code{x} at the
      index given by \code{after}.
    }
    \item{}{\code{c(x, ..., recursive = FALSE)}: Concatenates the
      \code{FilterRule} instances in \code{...} onto the end of \code{x}.
    }
  }
}

\section{Evaluating}{
  \describe{
    \item{}{
      \code{eval(expr, envir = parent.frame(),
        enclos = if (is.list(envir) || is.pairlist(envir))
        parent.frame() else baseenv())}: 
      Evaluates a \code{FilterRules} instance (passed as the
      \code{expr} argument). Expression rules are
      evaluated in \code{envir}, while function rules are invoked with
      \code{envir} as their only argument. The evaluation of a rule
      should yield a logical vector. The results from the rule
      evaluations are combined via the AND operation (i.e. \code{&}) so
      that a single logical vector is returned from \code{eval}.
    }
    \item{}{
      \code{evalSeparately(expr, envir = parent.frame(), enclos = if
        (is.list(envir) || is.pairlist(envir)) parent.frame() else
        baseenv())}: Evaluates separately each rule in a
      \code{FilterRules} instance (passed as the \code{expr}
      argument). Expression rules are evaluated in \code{envir}, while
      function rules are invoked with \code{envir} as their only
      argument. The evaluation of a rule should yield a logical
      vector. The results from the rule evaluations are combined into
      a logical matrix, with a column for each rule. This is
      essentially the parallel evaluator, while \code{eval} is the
      serial evaluator.
    }
    \item{}{
      \code{subsetByFilter(x, filter)}: Evaluates \code{filter} on
      \code{x} and uses the result to subset \code{x}. The result
      contains only the elements in \code{x} for which \code{filter}
      evaluates to \code{TRUE}.
    }
    \item{}{\code{summary(object, subject)}:
      Returns an integer vector with the number of elements
      in \code{subject} that pass each rule in \code{object}, along with
      a count of the elements that pass all filters.
    }
  }
}

\section{Filter Closures}{
  When a closure (function) is included as a filter in a
  \code{FilterRules} object, it is converted to a \code{FilterClosure},
  which is currently nothing more than a marker class that extends
  \code{function}. When a \code{FilterClosure} filter is extracted,
  there are some accessors and utilities for manipulating it:
  \describe{
    \item{}{\code{params}: Gets a named list of the objects that are
      present in the enclosing environment (without inheritance). This
      assumes that a filter is constructed via a constructor function,
      and the objects in the frame of the constructor (typically, the
      formal arguments) are the parameters of the filter.
    }
  }
}

\author{ Michael Lawrence }

\seealso{
  \link{FilterMatrix} objects for storing the logical output of a set
  of FilterRules objects.
}

\examples{
## constructing a FilterRules instance

## an empty set of filters
filters <- FilterRules()
  
## as a simple character vector
filts <- c("peaks", "promoters")
filters <- FilterRules(filts)
active(filters) # all TRUE

## with functions and expressions
filts <- list(peaks = expression(peaks), promoters = expression(promoters),
              find_eboxes = function(rd) rep(FALSE, nrow(rd)))
filters <- FilterRules(filts, active = FALSE)
active(filters) # all FALSE

## direct, quoted args (character literal parsed)
filters <- FilterRules(under_peaks = peaks, in_promoters = "promoters")
filts <- list(under_peaks = expression(peaks),
              in_promoters = expression(promoters))

## specify both exprs and additional args
filters <- FilterRules(filts, diffexp = de)

filts <- c("promoters", "peaks", "introns")
filters <- FilterRules(filts)

## evaluation
df <- DataFrame(peaks = c(TRUE, TRUE, FALSE, FALSE),
                promoters = c(TRUE, FALSE, FALSE, TRUE),
                introns = c(TRUE, FALSE, FALSE, FALSE))
eval(filters, df)
fm <- evalSeparately(filters, df)
identical(filterRules(fm), filters)
summary(fm)
summary(fm, percent = TRUE)
fm <- evalSeparately(filters, df, serial = TRUE)

## set the active state directly
  
active(filters) <- FALSE # all FALSE
active(filters) <- TRUE # all TRUE
active(filters) <- c(FALSE, FALSE, TRUE)
active(filters)["promoters"] <- TRUE # use a filter name
  
## toggle the active state by name or index
  
active(filters) <- c(NA, 2) # NA's are dropped
active(filters) <- c("peaks", NA) 
}

\keyword{classes}
\keyword{methods}
