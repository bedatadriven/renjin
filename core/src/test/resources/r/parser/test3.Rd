\name{bigglm}
\alias{bigglm}
\alias{bigglm.function}
\alias{bigglm.data.frame}
\alias{bigglm.SQLiteConnection}
\alias{bigglm.RODBC}
\alias{vcov.bigglm}
\alias{deviance.bigglm}
\alias{family.bigglm}
\alias{AIC.bigglm}
\alias{bigglm,ANY,DBIConnection-method}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{Bounded memory linear regression }
\description{
 \code{bigglm} creates a generalized linear model object that uses only
 \code{p^2} memory for \code{p} variables.
 }
\usage{
bigglm(formula, data, family=gaussian(),...)
\method{bigglm}{data.frame}(formula, data,...,chunksize=5000)
\method{bigglm}{function}(formula, data, family=gaussian(),
     weights=NULL, sandwich=FALSE, maxit=8, tolerance=1e-7,
     start=NULL,quiet=FALSE,...)
\method{bigglm}{RODBC}(formula, data, family=gaussian(),
      tablename, ..., chunksize=5000)
\S4method{bigglm}{ANY,DBIConnection}(formula, data, family=gaussian(),
tablename, ..., chunksize=5000)
\method{vcov}{bigglm}(object,dispersion=NULL, ...)
\method{deviance}{bigglm}(object,...)
\method{family}{bigglm}(object,...)
\method{AIC}{bigglm}(object,...,k=2)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{formula}{A model formula}
  \item{data}{See Details below. Method dispatch is on this argument}
  \item{family}{A glm family object}
  \item{chunksize}{Size of chunks for processng the data frame}
  \item{weights}{A one-sided, single term formula specifying weights}
  \item{sandwich}{\code{TRUE} to compute the Huber/White sandwich
    covariance matrix (uses \code{p^4} memory rather than \code{p^2})}
  \item{maxit}{Maximum number of Fisher scoring iterations}
  \item{tolerance}{Tolerance for change in coefficient (as multiple of
    standard error)}
  \item{start}{Optional starting values for coefficients. If
    \code{NULL}, \code{maxit} should be at least 2 as some quantities
    will not be computed on the first iteration}
  \item{object}{A \code{bigglm} object}
  \item{dispersion}{Dispersion parameter, or \code{NULL} to estimate}
  \item{tablename}{For the \code{SQLiteConnection} method, the name of a
    SQL table, or a string specifying a join or nested select}
  \item{k}{penalty per parameter for AIC}
  \item{quiet}{When \code{FALSE}, warn if the fit did not converge}
  \item{...}{Additional arguments}
}
\details{
  The \code{data} argument may be a function, a data frame, or a
  \code{SQLiteConnection} or \code{RODBC} connection object. 

  When it is a function the function must take a single argument
  \code{reset}. When this argument is \code{FALSE} it returns a data
  frame with the next chunk of data or \code{NULL} if no more data are
  available. When\code{reset=TRUE} it indicates that the data should be
  reread from the  beginning by subsequent calls. The chunks need not be
  the same size or in the same order when the data are reread, but the
  same data must be provided in total.  The \code{bigglm.data.frame}
  method gives an example of how such a function might be written,
  another is in the Examples below.

  The model formula must not contain any data-dependent terms, as these
  will not be consistent when updated.  Factors are permitted, but the
  levels of the factor must be the same across all data chunks (empty
  factor levels are ok).  Offsets are allowed (since version 0.8).

  The \code{SQLiteConnection} and \code{RODBC} methods loads only the
  variables needed for the model, not the whole table. The code in the
  \code{SQLiteConnection} method should work for other \code{DBI}
  connections, but I do not have any of these to check it with.
}

\value{
  An object of class \code{bigglm}
}
\references{Algorithm AS274  Applied Statistics (1992) Vol.41, 
No. 2 }

\seealso{\code{\link{biglm}}, glm}
\examples{
data(trees)
ff<-log(Volume)~log(Girth)+log(Height)
a <- bigglm(ff,data=trees, chunksize=10, sandwich=TRUE)
summary(a)

gg<-log(Volume)~log(Girth)+log(Height)+offset(2*log(Girth)+log(Height))
b <- bigglm(gg,data=trees, chunksize=10, sandwich=TRUE)
summary(b)

\dontrun{
## requires internet access
make.data<-function(urlname, chunksize,...){
      conn<-NULL
     function(reset=FALSE){
     if(reset){
       if(!is.null(conn)) close(conn)
       conn<<-url(urlname,open="r")
     } else{
       rval<-read.table(conn, nrows=chunksize,...)
       if (nrow(rval)==0) {
            close(conn)
            conn<<-NULL
            rval<-NULL
       }
       return(rval)
     }
  }
}

airpoll<-make.data("http://faculty.washington.edu/tlumley/NO2.dat",
        chunksize=150,
        col.names=c("logno2","logcars","temp","windsp",
                    "tempgrad","winddir","hour","day"))

b<-bigglm(exp(logno2)~logcars+temp+windsp,
         data=airpoll, family=Gamma(log),
         start=c(2,0,0,0),maxit=10)
summary(b)         
}
}
\keyword{regression}% at least one, from doc/KEYWORDS
