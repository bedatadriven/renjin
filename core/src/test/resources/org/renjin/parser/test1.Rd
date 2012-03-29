\name{components_ashape3d}
\alias{components_ashape3d}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{
Connected subsets computation
}
\description{
This function calculates and clusters the different connected components of the \eqn{\alpha}-shape of a given sample of points in the three-dimensional space.
}
\usage{
components_ashape3d(as3d, indexAlpha = 1)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{as3d}{
An object of class \code{"ashape3d"} that represents the \eqn{\alpha}-shape of a given sample of points in the three-dimensional space, see \code{\link{ashape3d}}.
}
  \item{indexAlpha}{
A single value or vector with the indexes of \code{as3d$alpha} that should be used for the computation, see Details. 
}
}
\details{
The function \code{components_ashape3d} computes the connected components of the \eqn{\alpha}-shape for each value of \eqn{\alpha} in \code{as3d$alpha[indexAlpha]} when \code{indexAlpha} is numeric.

If \code{indexAlpha="all"} or \code{indexAlpha="ALL"} then the function computes the connected components of the \eqn{\alpha}-shape for all values of \eqn{\alpha} in \code{as3d$alpha}.
}
\value{
If \code{indexAlpha} is a single value then the function returns a vector \code{v} of length equal to the sample size. For each sample point \code{i}, \code{v[i]} represents the label of the connected component to which the point belongs (for isolated points, \code{v[i]=-1}). The labels of the connected components are ordered by size where the largest one (in number of vertices) gets the smallest label which is one.

Otherwise \code{components_ashape3d} returns a list of vectors describing the connected components of the \eqn{\alpha}-shape for each selected value of \eqn{\alpha}.
}
\seealso{
\code{\link{ashape3d}}, \code{\link{plot.ashape3d}}
}
\examples{
T1 <- rtorus(1000, 0.5, 2)
T2 <- rtorus(1000, 0.5, 2, ct = c(2, 0, 0), rotx = pi/2)
x <- rbind(T1, T2)
alpha <- c(0.25, 2)
ashape3d.obj <- ashape3d(x, alpha = alpha)
plot(ashape3d.obj, indexAlpha = "all")

# Connected components of the alpha-shape for both values of alpha
comp <- components_ashape3d(ashape3d.obj, indexAlpha = "all")
class(comp)
# Number of components and points in each component for alpha=0.25
table(comp[[1]])
# Number of components and points in each component for alpha=2
table(comp[[2]])

# Plot the connected components for alpha=0.25
plot(ashape3d.obj, byComponents = TRUE, indexAlpha = 1)
}
% Add one or more standard keywords, see file 'KEYWORDS' in the
% R documentation directory.
\keyword{package }
