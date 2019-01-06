#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#

# Adapted from https://github.com/cran/polynom/blob/1.3-6/R/polybase.R

library(hamcrest)

Math.polynomial <-
function(x, ...)
{
    switch(.Generic,
           round = ,
           signif = ,
           floor = ,
           ceiling = ,
           trunc = polynomial(NextMethod(.Generic)),
           stop(paste(.Generic, "unsupported for polynomials")))
}



as.character.polynomial <- function(x, decreasing = FALSE, ...)
{
    p <- unclass(x)
    lp <- length(p) - 1
    names(p) <- 0:lp
    p <- p[p != 0]

    if(length(p) == 0) return("0")

    if(decreasing) p <- rev(p)

    signs <- ifelse(p < 0, "- ", "+ ")
    signs[1] <- if(signs[1] == "- ") "-" else ""

    np <- names(p)
    p <- as.character(abs(p))
    p[p == "1" & np != "0"] <- ""

    pow <- paste("x^", np, sep = "")
    pow[np == "0"] <- ""
    pow[np == "1"] <- "x"
    stars <- rep.int("*", length(p))
    stars[p == "" | pow == ""] <- ""
    paste(signs, p, stars, pow, sep = "", collapse = " ")
}

polynomial <-
function(coef = c(0, 1))
{
    a <- as.numeric(coef)
    while((la <- length(a)) > 1 && a[la] == 0) a <- a[-la]
    structure(a, class = "polynomial")
}


print.polynomial <-
function(x, digits = getOption("digits"), decreasing = FALSE, ...)
{
    p <- as.character.polynomial(signif(x, digits = digits),
                                 decreasing = decreasing)

    p
}


pr <- structure(c(-120, 274, -225, 85, -15, 1), class = "polynomial")

assertThat(print(pr), identicalTo("-120 + 274*x - 225*x^2 + 85*x^3 - 15*x^4 + x^5"))
