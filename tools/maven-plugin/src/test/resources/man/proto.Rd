\name{proto}
\alias{proto}
\alias{as.proto}
\alias{as.proto.environment}
\alias{as.proto.list}
\alias{as.proto.proto}
\alias{isnot.function}
\alias{is.proto}
\alias{$.proto}
\alias{$<-.proto}
\alias{with.proto}
\alias{str.proto}
\alias{.}
\alias{this}
\alias{.that}
\alias{that}
\alias{.super}
\alias{super}
\alias{print.instantiatedProtoMethod}
\title{Prototype object-based programming}
\description{
\code{proto} creates or modifies objects of the proto object
oriented system.
}
\usage{
proto(. = parent.env(envir), expr = {}, envir = 
		new.env(parent = parent.frame()), ..., funEnvir )
\method{as.proto}{list}(x, envir, parent, all.names = FALSE, ..., 
	funEnvir = envir, SELECT = function(x) TRUE)
isnot.function(x)
}

\arguments{
  \item{.}{the parent object of the new object.  May be a proto object
    or an environment.}
  \item{expr}{a series of statements enclosed in braces that define
    the variables and methods of the object.  Empty braces, the default,
    may be used
    if there are no variables or methods to add at this time.}
  \item{envir}{an existing prototype object or environment into which 
    the variables and methods defined in \code{expr} are placed.  
    If omitted a new object is created.}
  \item{funEnvir}{the environment of methods passed via \dots are automatically
    set to this environment.  Normally this argument is omitted, defaulting
    to \code{envir}; however, one can specify \code{FALSE} 
    to cause their environment to not be set or one can specify some 
    other environment or proto object to which their environment is to be set.}
  \item{x}{a list.}
  \item{parent}{a prototype object or environment which is to be used
    as the parent of the object.  If \code{envir} is specified then
    its parent is coerced to \code{parent}.}
  \item{\dots}{for \code{proto} these are components to be embedded in 
    the new object.  For \code{as.proto.list} these are arguments to
    pass to \code{proto} in the case that a new object is created.}
  \item{SELECT}{a function which given an object returns \code{TRUE} or
    \code{FALSE} such that only those for which \code{SELECT} returns
    \code{TRUE} are kept in the returned \code{proto} object.}
  \item{all.names}{only names not starting with a dot are copied unless
    all.names is TRUE.}
}

\details{
The \code{proto} class
is defined to be a subclass of the R \code{environment} class.
In particular this implies that \code{proto} objects have
single inheritance and mutable state as all environments do.
\code{proto} creates or modifies objects of this class.  It inserts
all variables and functions in \code{expr} and then in \code{\dots}
into \code{envir} setting the parent to \code{.}.  The environment of
all  functions inserted into the environment are set to that environment.
All such functions should 
have the receiver object as their first argument.
Conventionally this is \code{.} (i.e. a dot).  
Also \code{.that} and \code{.super}
variables are added to the environment.  These 
point to the object itself and its parent.
Note that \code{proto} can be used as a method and overridden
like any other method.  This allows objects to have object-specific
versions of \code{proto}.

\code{as.proto} is a generic with methods for environments, proto objects
and lists.

\code{as.proto.list} copies inserts a copy of each component, \code{el},
of the list \code{x} into the the environment or proto object
\code{envir} for which \code{FUN(el)} is \code{TRUE}.  Also, components
whose name begins with a dot, \code{.}, are not copied unless 
\code{all.names} is \code{TRUE} (and \code{FUN(el)} is \code{TRUE}).
The result is a proto object whose parent is \code{parent}.
If \code{envir} is omitted a new object is created through a call to
\code{proto} with \code{parent} and \code{\dots} as arguments.
If \code{parent} is also omitted then the current environment is used.
Note that if \code{parent} is a proto object with its own \code{proto}
method then this call to \code{proto} will be overridden by that method.

The utility function \code{isnot.function} is provided for use with
\code{as.proto.list} to facilitate the copying of variables only.

\code{$} can be used to access or set variables and methods in an
object.

When \code{$} is used for getting variables and methods, calls of
the form \code{obj$v} search for v in \code{obj} and if not found
search upwards through the ancestors of \code{obj} until found
unless the name \code{v} begins with two dots \code{..}.  In that case no
upward search is done.

If \code{meth} is a function then \code{obj$meth} is an object
of class \code{c("instantiatedProtoMethod", "function")} 
which is a \code{proto}
method with the first, i.e. proto slot, already filled in.
It is normally used in the
context of a call to a method, e.g. \code{obj$meth(x,y)}.
There also exists \code{print.instantiatedProtoMethod} 
for printing such objects.  Note that an instantiated proto method
is not the same as a proto method.  The first has its first slot 
filled in whereas the second does not.
If it is desired to actually return the method as a value not
in the context of a call then use the form 
\code{obj$with(meth)} or \code{obj[[meth]]} 
which are similar to \code{with(obj, meth)} except that 
the variation using \code{with} will search through ancestors while
\code{[[} will not search through ancestors).
The difference between \code{obj$meth} and \code{obj$with(meth)}
is that in the first case
\code{obj} implicitly provides the first argument to the
call so that \code{obj$meth(x,y)} and \code{obj$with(meth)(obj,x,y)}
are equivalent while in the case of \code{obj$with(meth)} the first
argument is not automatically inserted.  
\code{$.proto} also has a three argument form.  If the third argument
is present then it should be a list specifying the arguments at which
the instantiated method is to be evaluated.



The forms \code{.that$meth} and 
\code{.super$meth}
are special and should only be used within methods.  \code{.that}
refers to the object in which the current method is located
and \code{.super} refers to the parent of \code{.that}.  In both
cases the receiver object must be 
specified as the first argument --- the receiver
is not automatically inserted
as with other usages of \code{$}.

\code{$} can be used to set variables and methods in an object.
No ancestors are searched for the set form of \code{$}.
If the variable is the special variable \code{.super} then not only
is the variable set but the object's parent is set to \code{.super}.

A \code{with} method is available for \code{proto} objects.

\code{is.proto(p)} returns TRUE if p is a prototype object.

\code{str.proto} is provided for inspecting \code{proto} objects.

}

\value{
\code{proto} and \code{as.proto} all
return proto objects.  \code{isnot.function} returns a logical value.
}

\note{
proto methods can be used with environments but some care must be
taken.  These can be avoided by always using proto objects in these
cases.  This note discusses the pitfalls of using environments for
those cases where such interfacing is needed.

Note that if \code{e} is an environment then \code{e$x} will only
search for \code{x} in \code{e} and no further whereas if \code{e}
were a proto object its ancestors will be searched as well.
For example,
if the parent of a \code{proto} object is an \code{environment}
but not itself a \code{proto} object then \code{.super$x} references
in the methods of that object will only look as far as the parent.

Also note that the form \code{e$meth(...)} when used with an environment
will not automatically insert \code{e} as the first argument and
so environments can only be used with methods by using the more verbose
\code{e$meth(e, ...)}.  Even then it is not exactly equivalent since
\code{meth} will only be looked up in \code{e} but not its ancestors.
To get precise equivalence write the even more verbose
\code{with(e, meth)(e, ...)}.

If the user has a proto object \code{obj}
which is a child of the global environment and whose methods 
use \code{.super} then \code{.super} will refer to an environment,
not a proto object (unless the global environment is coerced to
a proto object) and therefore be faced with the search situation
discussed above.  One solution is to
create an empty root object between the global environment
and \code{obj} like this \code{Root <- obj$.super <- proto(.GlobalEnv)}
where \code{Root} is the root object.
Now \code{.super} references will reference \code{Root}, which is
a proto object so search will occur as expected.  \code{proto}
does not provide such a root object automatically
but the user can create one easily as shown, if desired.

Although not recommended, it possible to coerce the global environment
to a proto object by issuing the command
\code{as.proto(.GlobalEnv)}.  This will effectively make the global
environment a proto root object
but has the potential to break other software, although the 
authors have not actually found any software that it breaks.
}

\seealso{\code{\link{as.list}}, \code{\link{names}}, \code{\link{environment}}}

\examples{
oo <- proto(expr = {x = c(10, 20, 15, 19, 17)
             location <- function(.) mean(.$x) # 1st arg is object
             rms <- function(.) 
                      sqrt(mean((.$x - .$location())^2))
             bias <- function(., b) .$x <- .$x + b
})

oo2 <- oo$proto( location = function(.) median(.$x) )
oo2$rms()      # note that first argument is omitted.
oo2$ls()       # list components of oo2
oo2$as.list()  # contents of oo2 as a list
oo2            # oo2 itself
oo2$parent.env() # same
oo2$parent.env()$as.list() # contents of parent of oo2
oo2$print()
oo2$ls()
oo2$str()
oo3 <- oo2
oo2$identical(oo3)
oo2$identical(oo)

# start off with Root to avoid problem cited in Note
Root <- proto()
oop <- Root$proto(a = 1, incr = function(.) .$a <- .$a+1)
ooc <- oop$proto(a = 3) # ooc is child of oop but with a=3
ooc$incr()
ooc$a      # 4

# same but proto overridden to force a to be specified
oop$proto <- function(., a) { .super$proto(., a=a) }
\dontrun{
ooc2 <- oop$launchMissile() # DO NOT RUN FOR THE LOVE OF 
}
ooc2 <- oop$proto(a = 10) 
ooc2$incr()
ooc2$a # 11

# use of with to eliminate having to write .$a
o2 <- proto(a = 1, incr = function(.) with(., a <- a+1))
o2c <- as.proto(o2$as.list()) # o2c is a clone of o2
o2d <- o2$proto()  # o2d is a child of o2
o2$a <- 2
o2c$a  # a not changed by assignment in line above
o2d$a # a is changed since a not found in o2d so found in o2

p <- proto(a = 0, incr = function(., x) .$a <- .$a + x)
pc <- p$proto(a = 100)
sapply(list(p, pc), "$.proto", "incr", list(x = 7))

}
\keyword{programming}
