\name{p.hboxp}
\alias{p.hboxp}
\title{Add a Horizontal Boxplot to the Current Plot}
\description{
  Add a horizontal boxplot to the current plot.  This is mainly an
  auxiliary function for \code{\link{hist.bxp}}, since
  \code{\link{boxplot}(*, horizontal = TRUE, add = TRUE)} is usually
  much preferable to this.
}
\usage{
p.hboxp(x, y.lo, y.hi, boxcol = 3,
        medcol = 2, medlwd = 5, whisklty = 2, staplelty = 1)
}
\arguments{
  \item{x}{univariate data set.}
  \item{y.lo, y.hi}{minimal and maximal \emph{user} coordinates
    \bold{or} \code{y.lo = c(ylo,hyi)}.}
  \item{boxcol, medcol}{color of the box and the median line.}
  \item{medlwd}{line width of median line.}
  \item{whisklty, staplelty}{line types of the whisker and the staple,
    the latter being used for the outmost non-outliers.}
}
\details{
....
}
\author{Martin Maechler building on code from Markus and Christian Keller.}
\seealso{\code{\link{boxplot}(**, horizontal = TRUE, add= TRUE)}.}
\examples{
%% FIXME: add!
  ## ==>  See code in 'hist.bxp' (.)  and  example(hist.bxp) !
  ##
}
\keyword{aplot}
