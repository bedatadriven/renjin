\name{List-class}
\docType{class}

% List class, functions and methods:
\alias{class:List}
\alias{List-class}
\alias{List}

\alias{elementType}
\alias{elementType,List-method}
\alias{elementType,vector-method}
\alias{elementNROWS}
\alias{elementNROWS,ANY-method}
\alias{elementNROWS,List-method}
\alias{elementLengths}
\alias{elementLengths,ANY-method}
\alias{isEmpty}
\alias{isEmpty,ANY-method}
\alias{isEmpty,List-method}

\alias{relistToClass}
\alias{relistToClass,ANY-method}

\alias{coerce,List,list-method}
\alias{coerce,ANY,List-method}
\alias{coerce,integer,List-method}
\alias{as.list,List-method}
\alias{as.env,List-method}
\alias{unlist,List-method}
\alias{as.data.frame,List-method}
\alias{coerce,List,data.frame-method}

\alias{[,List-method}
\alias{[<-,List-method}
\alias{[[,List-method}
\alias{[[<-,List-method}
\alias{$,List-method}
\alias{$<-,List-method}

\alias{show,List-method}


\title{List objects}

\description{
  List objects are \link{Vector} objects with a \code{"[["},
  \code{elementType} and \code{elementNROWS} method.
  The List class serves a similar role as \link[base]{list} in base R.

  It adds one slot, the \code{elementType} slot, to the two slots shared by
  all \link{Vector} objects.

  The \code{elementType} slot is the preferred location for List
  subclasses to store the type of data represented in the sequence. It is
  designed to take a character of length 1 representing the class of the
  sequence elements. While the List class performs no validity checking
  based on \code{elementType}, if a subclass expects elements to be of a
  given type, that subclass is expected to perform the necessary validity
  checking. For example, the subclass \link[IRanges]{IntegerList} (defined
  in the \pkg{IRanges} package) has \code{elementType = "integer"} and its
  validity method checks if this condition is TRUE.

  To be functional, a class that inherits from List must define at least
  a \code{"[["} method (in addition to the minimum set of \link{Vector}
  methods).
}

\section{Construction}{
  List objects are typically constructed using one of the 3 following methods:
  \itemize{
    \item Use of a constructor function. Many constructor functions are
          provided for List objects e.g. \code{List},
          \code{\link[IRanges]{IntegerList}}, \code{\link[IRanges]{RleList}},
          \code{\link[IRanges]{RangesList}},
          \code{\link[GenomicRanges]{GRangesList}}, etc...
          Which one to use depends on the particular type of List object
          to construct. The name of a constructor function is always
          the name of a valid class. If it's the name of a \emph{concrete}
          class (e.g. the \code{\link[GenomicRanges]{GRangesList}} constructor
          defined in the \pkg{GenomicRanges} package), then the constructor
          function returns an instance of that class. If it's the name of a
          \emph{virtual} class (e.g. the \code{List} constructor defined in
          this package, or the \code{\link[IRanges]{IntegerList}} or
          \code{\link[IRanges]{RleList}} or \code{\link[IRanges]{RangesList}}
          constructors defined in the \pkg{IRanges} package), then the returned
          object belongs to a concrete subclass of that virtual class. Which
          subclass exactly depends on each constructor function (see man page
          of a particular constructor function for the details).

    \item Coercion to List or to a List subclass. Many coercion methods are
          provided to turn any object into a List object. One general and
          convenient way to convert any vector-like object into a List is to
          call \code{as(x, "List")}. This will typically yield an object from
          a subclass of \link[IRanges]{CompressedList}.

    \item Use of \code{\link[IRanges]{extractList}}. This function, defined
          in the \pkg{IRanges} package, extracts user-specified groups of
          elements from a vector-like object and returns them in a List (or
          sometimes list) object.
  }
}

\section{Accessors}{
  In the following code snippets, \code{x} is a List object.
  
  \describe{
    \item{}{
      \code{length(x)}:
      Get the number of list elements in \code{x}.
    }
    \item{}{
      \code{names(x)}, \code{names(x) <- value}:
      Get or set the names of the elements in the List.
    }
    \item{}{
      \code{mcols(x, use.names=FALSE)}, \code{mcols(x) <- value}:
      Get or set the metadata columns. See \link{Vector} man page for
      more information.
    }
    \item{}{
      \code{elementType(x)}:
      Get the scalar string naming the class from which all elements must
      derive.
    }
    \item{}{
      \code{elementNROWS(x)}:
      Get the length (or nb of row for a matrix-like object) of each of
      the elements. Equivalent to \code{sapply(x, NROW)}.
    }
    \item{}{
      \code{elementLengths(x)}:
      Equivalent to \code{elementNROWS(x)}. Clearly a misnomer. Will be
      deprecated soon.
    }
    \item{}{
      \code{isEmpty(x)}:
      Returns a logical indicating either if the sequence has no elements
      or if all its elements are empty.
    }
  }
}

\section{Coercion}{
  To List.
  \describe{
    \item{}{
      \code{as(x, "List")}: Converts a vector-like object into a
      List, usually a \link[IRanges]{CompressedList} derivative.
      One notable exception is when \code{x} is an ordinary list,
      in which case \code{as(x, "List")} returns a \link{SimpleList}
      derivative.

      To explicitly request a \link{SimpleList} derivative, call
      \code{as(x, "SimpleList")}.

      See \code{?\link[IRanges]{CompressedList}} (you might need to load
      the \pkg{IRanges} package first) and \code{?\link{SimpleList}} for
      more information about the CompressedList and SimpleList
      representations.
    }
  }

  From List. In the code snippets below, \code{x} is a List object.
  \describe{
    \item{}{
      \code{as.list(x, ...)}, \code{as(from, "list")}:
      Turns \code{x} into an ordinary list.
    }
    \item{}{
      \code{unlist(x, recursive=TRUE, use.names=TRUE)}:
      Concatenates the elements of \code{x} into a single vector-like
      object (of class \code{elementType(x)}).
    }
    \item{}{
      \code{as.data.frame(x, row.names=NULL, optional=FALSE ,
                          value.name="value", use.outer.mcols=FALSE, 
                          group_name.as.factor=FALSE, ...)}: 
      Coerces a \code{List} to a \code{data.frame}. The result has the
      same length as unlisted \code{x} with two additional columns,
      \code{group} and \code{group_name}. \code{group} is an \code{integer}
      that indicates which list element the record came from. 
      \code{group_name} holds the list name associated with each
      record; value is \code{character} by default and \code{factor} when 
      \code{group_name.as.factor} is TRUE. 

      When \code{use.outer.mcols} is TRUE the metadata columns on the 
      outer list elements of \code{x} are replicated out and included 
      in the \code{data.frame}. List objects that unlist to a 
      single vector (column) are given the column name `value` by default.
      A custom name can be provided in \code{value.name}.

      Splitting values in the resulting \code{data.frame} by the original 
      groups in \code{x} should be done using the \code{group} column as 
      the \code{f} argument to \code{splitAsList}. To relist data, use
      \code{x} as the \code{skeleton} argument to \code{relist}.
    }
    \item{}{
      \code{as.env(x, enclos = parent.frame())}:
      Creates an environment from \code{x} with a symbol for each
      \code{names(x)}. The values are not actually copied into the
      environment. Rather, they are dynamically bound using
      \code{\link{makeActiveBinding}}. This prevents unnecessary copying
      of the data from the external vectors into R vectors. The values
      are cached, so that the data is not copied every time the symbol
      is accessed.
    }
  }
}

\section{Subsetting}{
  In the code snippets below, \code{x} is a List object.

  \describe{
    \item{}{
      \code{x[i]}:
      Return a new List object made of the list elements selected by
      subscript \code{i}. Subscript \code{i} can be of any type supported
      by subsetting of a Vector object (see \link{Vector} man page for the
      details), plus the following types: \link[IRanges]{IntegerList},
      \link[IRanges]{LogicalList}, \link[IRanges]{CharacterList},
      integer-\link[IRanges]{RleList}, logical-\link[IRanges]{RleList},
      character-\link[IRanges]{RleList}, and \link[IRanges]{RangesList}.
      Those additional types perform subsetting within the list elements
      rather than across them.
    }
    \item{}{
      \code{x[i] <- value}:
      Replacement version of \code{x[i]}.
    }
    \item{}{
      \code{x[[i]]}:
      Return the selected list element \code{i}, where \code{i} is an
      numeric or character vector of length 1.
    }
    \item{}{
      \code{x[[i]] <- value}:
      Replacement version of \code{x[[i]]}.
    }
    \item{}{
      \code{x$name}, \code{x$name <- value}:
      Similar to \code{x[[name]]} and \code{x[[name]] <- value}, but
      \code{name} is taken literally as an element name.
    }
  }
}

\section{relistToClass}{
  \code{relistToClass(x)} is the opposite of \code{elementType(y)} in the
  sense that the former returns the class of the result of relisting (or
  splitting) \code{x} while the latter returns the class of the result of
  unlisting (or unsplitting) \code{y}.
  More formally, if \code{x} is an object that is relistable and \code{y}
  a list-like object:
\preformatted{    relistToClass(x) is class(relist(x, some_skeleton))
    elementType(y) is class(unlist(y))
}
  As a consequence, for any object \code{x} for which \code{relistToClass(x)}
  is defined and returns a valid class,
  \code{elementType(new(relistToClass(x)))} should return \code{class(x)}.
}

\author{P. Aboyoun and H. Pagès}

\seealso{
  \itemize{
    \item \link{List-utils} for common operations on List objects.

    \item \link{Vector} objects for the parent class.

    \item The \link{SimpleList} class for a direct extension of the List class.

    \item The \link[IRanges]{CompressedList} class defined in the
          \pkg{IRanges} package for another direct extension of the List class.

    \item The \link[IRanges]{IntegerList}, \link[IRanges]{RleList}, and
          \link[IRanges]{IRanges} classes and constructors defined in the
          \pkg{IRanges} package for more examples of concrete List
          subclasses.

    \item The \link[IRanges]{extractList} function defined in the
          \pkg{IRanges} package for grouping elements of a vector-like
          object into a list-like object.
  }
}

\examples{
showClass("List")  # shows (some of) the known subclasses
}

\keyword{methods}
\keyword{classes}
