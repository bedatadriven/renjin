\name{generate.Guilds}
\alias{generate.Guilds}
\title{
  Generate Artificial data under the GUILDS model
}
\description{
 Using this function it is possible to generate a community dataset consisting of two separate abundance vectors for each guild, where the data generated adhere to the Guilds model.
}
\usage{
generate.Guilds(theta, alpha_x, alpha_y, J)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{theta}{ Fundamental Biodiversity Number theta

}
  \item{alpha_x}{ Dispersal Ability of Guild X

}
  \item{alpha_y}{Dispersal Ability of Guild Y
}
  \item{J}{Total number of individuals in the local community (e.g. J_X + J_Y).
}
}

\value{
    \item{guildX}{Vector containing the unlabeled abundances of species in Guild X}
    \item{guildY}{Vector containing the unlabeled abundances of species in Guild Y}
}

\author{
 Thijs Janzen
}

\examples{
generate.Guilds(theta=200,alpha_x = 0.005, alpha_y = 0.001,J=20000);% Add one or more standard keywords, see file 'KEYWORDS' in the
}