\name{Hits-class}
\docType{class}

\alias{class:Hits}
\alias{Hits-class}
\alias{Hits}

\alias{class:SelfHits}
\alias{SelfHits-class}
\alias{SelfHits}

\alias{class:SortedByQueryHits}
\alias{SortedByQueryHits-class}
\alias{SortedByQueryHits}

\alias{class:SortedByQuerySelfHits}
\alias{SortedByQuerySelfHits-class}
\alias{SortedByQuerySelfHits}

\alias{parallelSlotNames,Hits-method}

% accessors
\alias{from}
\alias{from,Hits-method}
\alias{to}
\alias{to,Hits-method}
\alias{nLnode}
\alias{nLnode,Hits-method}
\alias{nRnode}
\alias{nRnode,Hits-method}
\alias{nnode}
\alias{nnode,SelfHits-method}
\alias{countLnodeHits}
\alias{countLnodeHits,Hits-method}
\alias{countRnodeHits}
\alias{countRnodeHits,Hits-method}

\alias{queryHits}
\alias{subjectHits}
\alias{queryLength}
\alias{subjectLength}
\alias{countQueryHits}
\alias{countSubjectHits}

% updateObject
\alias{updateObject,Hits-method}

% coercion
\alias{coerce,Hits,SortedByQueryHits-method}
\alias{as.matrix,Hits-method}
\alias{as.table,Hits-method}

% subsetting
\alias{extractROWS,classNameForDisplay,ANY-method}

% displaying
\alias{classNameForDisplay,Hits-method}
\alias{classNameForDisplay,SelfHits-method}
\alias{show,Hits-method}

% commbining
\alias{c,Hits-method}

% other transformations
\alias{t,Hits-method}
\alias{remapHits}

% SelfHits
\alias{isSelfHit}
\alias{isRedundantHit}

\title{Hits objects}

\description{
  The Hits class is a container for representing a set of hits between
  a set of \emph{left nodes} and a set of \emph{right nodes}.
  Note that only the hits are stored in the object. No information about
  the left or right nodes is stored, except their number.

  For example, the \code{\link[IRanges]{findOverlaps}} function, defined
  and documented in the \pkg{IRanges} package, returns the hits between
  the \code{query} and \code{subject} arguments in a \code{Hits} object.
}

\usage{
## Constructor functions

Hits(from=integer(0), to=integer(0), nLnode=0L, nRnode=0L, ...,
     sort.by.query=FALSE)

SelfHits(from=integer(0), to=integer(0), nnode=0L, ...,
     sort.by.query=FALSE)
}

\arguments{
  \item{from, to}{
    2 integer vectors of the same length.
    The values in \code{from} must be >= 1 and <= \code{nLnode}.
    The values in \code{to} must be >= 1 and <= \code{nRnode}.
  }
  \item{nLnode, nRnode}{
    Number of left and right nodes.
  }
  \item{...}{
    Metadata columns to set on the Hits object. All the metadata columns must
    be vector-like objects of the same length as \code{from} and \code{to}.
  }
  \item{sort.by.query}{
    Should the hits in the returned object be sorted by query? If yes, then
    a SortedByQueryHits object is returned (SortedByQueryHits is a subclass
    of Hits).
  }
  \item{nnode}{
    Number of nodes.
  }
}

\section{Accessors}{
  In the code snippets below, \code{x} is a \code{Hits} object.
  \describe{
    \item{}{\code{length(x)}: get the number of hits}
    \item{}{\code{from(x)}: Equivalent to \code{as.data.frame(x)[[1]]}.}
    \item{}{\code{to(x)}: Equivalent to \code{as.data.frame(x)[[2]]}.}
    \item{}{\code{nLnode(x)}, \code{nrow(x)}: get the number of left nodes}
    \item{}{\code{nRnode(x)}, \code{ncol(x)}: get the number of right nodes}
    \item{}{\code{countLnodeHits(x)}: Counts the number of hits for
      each left node, returning an integer vector.
    }
    \item{}{\code{countRnodeHits(x)}: Counts the number of hits for
      each right node, returning an integer vector.
    }
  }

  The following accessors are just aliases for the above accessors:
  \describe{
    \item{}{\code{queryHits(x)}: alias for \code{from(x)}.}
    \item{}{\code{subjectHits(x)}: alias for \code{to(x)}.}
    \item{}{\code{queryLength(x)}: alias for \code{nLnode(x)}.}
    \item{}{\code{subjectLength(x)}: alias for \code{nRnode(x)}.}
    \item{}{\code{countQueryHits(x)}: alias for \code{countLnodeHits(x)}.}
    \item{}{\code{countSubjectHits(x)}: alias for \code{countRnodeHits(x)}.}
  }
}

\section{Coercion}{
  In the code snippets below, \code{x} is a \code{Hits} object.
  \describe{
    \item{}{\code{as.matrix(x)}: Coerces \code{x} to a two
      column integer matrix, with each row representing a hit
      between a left node (first column) and a right node (second
      column).
    }
    \item{}{\code{as.table(x)}: Counts the number of hits for
      each left node in \code{x} and outputs the counts as a \code{table}.
    }
  }
}

\section{Subsetting}{
  In the code snippets below, \code{x} is a \code{Hits} object.
  \describe{
    \item{}{\code{x[i]}: Subset the Hits object.}
  }
}

\section{Other transformations}{
  In the code snippets below, \code{x} is a \code{Hits} object.
  \describe{
    \item{}{\code{t(x)}:
      Transpose \code{x} by interchanging the left and right nodes. This
      allows, for example, counting the number of hits for each right node
      using \code{as.table}.
    }
    \item{}{\code{remapHits(x, Lnodes.remapping=NULL, new.nLnode=NA,
                               Rnodes.remapping=NULL, new.nRnode=NA)}:
      Only supports SortedByQueryHits objects at the moment.

      Remaps the left and/or right nodes in \code{x}. The left nodes are
      remapped thru the map specified via the \code{Lnodes.remapping} and
      \code{new.nLnode} arguments. The right nodes are remapped thru the
      map specified via the \code{Rnodes.remapping} and \code{new.nRnode}
      arguments.

      \code{Lnodes.remapping} must represent a function defined on the
      1..M interval that takes values in the 1..N interval, where N is
      \code{nLnode(x)} and M is the value specified by the user via the
      \code{new.nLnode} argument. Note that this mapping function doesn't
      need to be injective or surjective. Also it is not represented by an R
      function but by an integer vector of length M with no NAs. More precisely
      \code{Lnodes.remapping} can be NULL (identity map), or a vector of
      \code{nLnode(x)} non-NA integers that are >= 1 and
      <= \code{new.nLnode}, or a factor of length \code{nLnode(x)}
      with no NAs (a factor is treated as an integer vector, and, if missing,
      \code{new.nLnode} is taken to be its number of levels). Note that
      a factor will typically be used to represent a mapping function that is
      not injective.

      The same applies to the \code{Rnodes.remapping}.

      \code{remapHits} returns a Hits object where \code{from(x)} and
      \code{to(x)} have been remapped thru the 2 specified maps. This
      remapping is actually only the 1st step of the transformation, and is
      followed by 2 additional steps: (2) the removal of duplicated hits,
      and (3) the reordering of the hits (first by query hits, then by subject
      hits). Note that if the 2 maps are injective then the remapping won't
      introduce duplicated hits, so, in that case, step (2) is a no-op (but
      is still performed). Also if the "query map" is strictly ascending and
      the "subject map" ascending then the remapping will preserve the order
      of the hits, so, in that case, step (3) is also a no-op (but is still
      performed).
    }
  }
}

\section{SelfHits}{
  A SelfHits object is a Hits object where the left and right nodes are
  identical. For a SelfHits object \code{x}, \code{nLnode(x)} is equal to
  \code{nRnode(x)}. The object can be seen as an oriented graph where
  \code{nLnode} is the nb of nodes and the hits are the (oriented) edges.
  SelfHits objects support the same set of accessors as Hits objects
  plus the \code{nnode()} accessor that is equivalent to \code{nLnode()}
  and \code{nRnode()}.

  We also provide two little utilities to operate on a SelfHits object
  \code{x}:
  \describe{
    \item{}{\code{isSelfHit(x)}: A \emph{self hit} is an edge from a node to
      itself. \code{isSelfHit(x)} returns a logical vector \emph{parallel}
      to \code{x} indicating which elements in \code{x} are self hits.
    }
    \item{}{\code{isRedundantHit(x)}: When there is more than 1 edge between
      2 given nodes (regardless of orientation), the extra edges are considered
      to be \emph{redundant hits}. \code{isRedundantHit(x)} returns a logical
      vector \emph{parallel} to \code{x} indicating which elements in \code{x}
      are redundant hits.
    }
  }
}

\author{Michael Lawrence and Hervé Pagès}

\seealso{
  \itemize{
    \item \link{Hits-comparison} for comparing and ordering hits.

    \item The \code{\link[IRanges]{findOverlaps}} function in the
          \pkg{IRanges} package which returns SortedByQueryHits object.

    \item \link[IRanges]{Hits-examples} in the \pkg{IRanges} package, for
          some examples of Hits object basic manipulation.

    \item \link[IRanges]{setops-methods} in the \pkg{IRanges} package,
          for set operations on Hits objects.
  }
}

\examples{
from <- c(5, 2, 3, 3, 3, 2)
to <- c(11, 15, 5, 4, 5, 11)
id <- letters[1:6]

Hits(from, to, 7, 15, id)
Hits(from, to, 7, 15, id, sort.by.query=TRUE)

## ---------------------------------------------------------------------
## selectHits()
## ---------------------------------------------------------------------

x <- c("a", "b", "a", "c", "d")
table <- c("a", "e", "d", "a", "a", "d")
hits <- findMatches(x, table)  # sorts the hits by query
hits

selectHits(hits, select="all")  # no-op
selectHits(hits, select="first")
selectHits(hits, select="last")
selectHits(hits, select="arbitrary")
selectHits(hits, select="count")

## ---------------------------------------------------------------------
## remapHits()
## ---------------------------------------------------------------------

Lnodes.remapping <- factor(c(a="A", b="B", c="C", d="D")[x],
                           levels=LETTERS[1:4])
remapHits(hits, Lnodes.remapping=Lnodes.remapping)

## See ?`Hits-examples` in the IRanges package for more examples of basic
## manipulation of Hits objects.

## ---------------------------------------------------------------------
## SelfHits objects
## ---------------------------------------------------------------------

hits2 <- SelfHits(c(2, 3, 3, 3, 3, 3, 4, 4, 4), c(4, 3, 2:4, 2, 2:3, 2), 4)
## Hits 2 and 4 are self hits (from 3rd node to itself):
which(isSelfHit(hits2))
## Hits 4, 6, 7, 8, and 9, are redundant hits:
which(isRedundantHit(hits2))

hits3 <- findMatches(x)
hits3[!isSelfHit(hits3)]
hits3[!(isSelfHit(hits3) | isRedundantHit(hits3))]
}
\keyword{methods}
\keyword{classes}
