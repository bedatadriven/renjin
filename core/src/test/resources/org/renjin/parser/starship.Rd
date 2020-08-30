\name{starship}
\alias{starship}
\title{Carry out the ``starship'' estimation method for the generalised
lambda distribution} 
\description{
Estimates parameters of the generalised lambda
distribution on the basis of data, using the starship method.
The starship method is built on the fact that the 
generalised lambda distribution (\code{\link{gld}})
is a transformation of the uniform distribution.  This method finds the 
parameters that transform the data closest to the uniform distribution.
This function uses a grid-based search to find a suitable starting point (using
\code{\link{starship.adaptivegrid}}) then uses \code{\link{optim}} to find
the parameters that do this.
}
\usage{
starship(data, optim.method = "Nelder-Mead", initgrid = NULL, 
inverse.eps = .Machine$double.eps, param="FMKL", optim.control=NULL, return.data=FALSE)
}
\arguments{
  \item{data}{Data to be fitted, as a vector}
  \item{optim.method}{Optimisation method for \code{\link{optim}} to use, 
defaults to Nelder-Mead}
  \item{initgrid}{Grid of values of \eqn{\lambda_3}{lambda 3} and
\eqn{\lambda_4}{lambda 4}
to try, in \code{\link{starship.adaptivegrid}}.  This should be a list with
elements, 
  \code{lcvect}, a vector of values for \eqn{\lambda_3}{lambda 3},
  \code{ldvect}, a vector of values for \eqn{\lambda_4}{lambda 4} and
  \code{levect}, a vector of values for \eqn{\lambda_5}{lambda 5} 
  (\code{levect} is only required if \code{param} is \code{fm5}).
  
If it is left as NULL, the default grid depends on the parameterisation.
For \code{fmkl}, both lcvect and ldvect default to:
\tabular{ccccccccccc}{-1.5\tab -1\tab -0.5\tab -0.1\tab 0\tab 0.1\tab 0.2\tab 0.4\tab 0.8\tab 1\tab 1.5}
(\code{levect} is NULL).

For \code{rs}, both lcvect and ldvect default to:
\tabular{cccccc}{0.1\tab 0.2\tab 0.4\tab 0.8\tab 1\tab 1.5}
(\code{levect} is NULL).  Note that this restricts the estimates to only part of the region
of the  \eqn{\lambda_3}{lambda 3}, \eqn{\lambda_4}{lambda 4} plane.

For \code{gpd}, the defaults are: \eqn{\delta}: \tabular{ccc}{0.3\tab 0.5\tab 0.7} and \eqn{\lambda}: \tabular{cccccccc}{-1.5\tab-.5\tab0\tab.2\tab.4\tab0.8\tab1.5\tab5}

For \code{fm5}, both lcvect and ldvect default to:
\tabular{ccccccccccc}{-1.5\tab -1\tab -.5\tab -0.1\tab 0\tab 0.1\tab 0.2\tab 0.4\tab 0.8\tab 1\tab 1.5}
and \code{levect} defaults to:
\tabular{ccccc}{-0.5\tab 0.25\tab 0\tab 0.25\tab 0.5}
	}
\item{inverse.eps}{Accuracy of calculation for the numerical determination of 
	\eqn{F(x)}, defaults to \code{.Machine$double.eps}}
\item{param}{choose parameterisation: 
  \code{fmkl} uses \emph{Freimer, Mudholkar, Kollia and Lin (1988)} (default).
  \code{rs} uses \emph{Ramberg and Schmeiser (1974)}
  \code{fm5} uses the 5 parameter version of the FMKL parameterisation 
  (paper to appear)}
\item{optim.control}{List of options for the optimisation step.  See
\code{\link{optim}} for details.  If left as NULL, the parscale 
control is set to scale \eqn{\lambda_1}{lambda 1}
and \eqn{\lambda_2}{lambda 2} by the absolute value of their starting points.  }
\item{return.data}{Logical: Should the function return the data (from the argument \code{data})?}
}
\details{
The starship method is described in King & MacGillivray, 1999 (see
references). It is built on the fact that the 
generalised lambda distribution (\code{\link{gld}})
is a transformation of the uniform distribution.  Thus the inverse of this
transformation is the distribution function for the gld.  The starship method
applies different values of the parameters of the distribution to the
distribution function, calculates the depths \emph{q} corresponding to the data
and chooses the parameters that make the depths closest to a uniform
distribution.

The closeness to the uniform is assessed by calculating the Anderson-Darling
goodness-of-fit test on the transformed data against the uniform, for a
sample of size \code{length(data)}.

This is implemented in 2 stages in this function.  First a grid search is
carried out, over a small number of possible parameter values
(see \code{\link{starship.adaptivegrid}} for details).  Then the minimum from
this search is given as a starting point for an optimisation of the 
Anderson-Darling value using optim, with method given by \code{optim.method}

See \code{\link{GeneralisedLambdaDistribution}} for details on
parameterisations.
}
\value{
\code{starship} returns an object of \code{\link[base]{class}} \code{"starship"}.

\code{print} prints the estimated values of the parameters, while
\code{\link[gld]{summary.starship}} prints these by default, but can also provide
details of the estimation process (from the components \code{grid.results} and 
\code{optim} detailed below).

An object of class \code{"starship"} is a list containing at least the
following components:

  \item{lambda}{A vector of length 4 (or 5, for the \emph{fm5} parameterisation), giving 
the estimated parameters, in order,
  \eqn{\lambda_1}{lambda 1} - location parameter
  \eqn{\lambda_2}{lambda 2} - scale parameter
  \eqn{\lambda_3}{lambda 3} - first shape parameter
  \eqn{\lambda_4}{lambda 4} - second shape parameter
(See \code{\link[gld]{gld}} for details of the parameters in the \emph{fm5} parameterisation)
  
  In the \emph{gpd} parameterisation, the parameters are labelled:
  \eqn{\alpha}{alpha} - location parameter
  \eqn{\beta}{beta} - scale parameter
  \eqn{\delta}{delta} - skewness parameter
  \eqn{\lambda}{lambda} - tailweight parameter  
  }
  \item{grid.results}{output from the grid search - see
\code{\link{starship.adaptivegrid}} for details}
  \item{optim}{output from the optim search -
\code{\link{optim}} for details}
}
\references{ Freimer, M., Mudholkar, G. S., Kollia, G. & Lin, C. T. (1988), 
	\emph{A study of the generalized tukey lambda family}, Communications 
	in Statistics - Theory and Methods \bold{17}, 3547--3567.

Ramberg, J. S. & Schmeiser, B. W. (1974), \emph{An approximate method for
generating asymmetric random variables}, Communications of the ACM \bold{17}, 
78--82.
 
King, R.A.R. & MacGillivray, H. L. (1999), \emph{A starship method for
fitting the generalised \eqn{\lambda}{lambda} distributions},
Australian and New Zealand Journal of
Statistics \bold{41}, 353--374

Owen, D. B. (1988), \emph{The starship}, Communications in Statistics - 
Computation and Simulation \bold{17}, 315--323.

\url{http://tolstoy.newcastle.edu.au/~rking/gld/}
}
\author{Robert King, \email{robert.king@newcastle.edu.au},
\url{http://tolstoy.newcastle.edu.au/~rking/}

Darren Wraith}
\seealso{ 
\code{\link{starship.adaptivegrid}}, 
\code{\link{starship.obj}} 
}
\examples{
data <- rgl(100,0,1,.2,.2)
starship(data,optim.method="Nelder-Mead",initgrid=list(lcvect=(0:4)/10,
ldvect=(0:4)/10))
}
\keyword{distribution}
