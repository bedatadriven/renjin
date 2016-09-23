#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

library(hamcrest)

test.Bquote <- function() {
	model <-  bquote(~0 + .(quote(births)))
	
	assertThat(model, equalTo( ~0 + births ) )
}

test.Namespaces <- function() {
	assertThat(typeof(stats::runif), equalTo("closure"))
}

test.BaseNamespaceEnv <- function() {
	baseNamespace <- environment(ls)
	
	assertThat(typeof(baseNamespace$.BaseNamespaceEnv), equalTo("environment"))
	assertThat(typeof(baseenv()$.BaseNamespaceEnv), equalTo("environment"))
}

test.Xtfrm <- function() {
	assertThat(xtfrm(1:10), identicalTo(1:10))
#	assertThat(xtfrm(c("a","b")), identicalTo(c(1L,2L)))
}

test.VapplyWithMatrixResult <- function() {
	res <- vapply(list(c(1L,2L,NA_integer_),c(1L,2L,NA_integer_)), is.na, c(NA,NA,NA))
	print(dim(res))
	assertThat( res, identicalTo( matrix(c(FALSE,FALSE,TRUE,FALSE,FALSE,TRUE), nrow=3)) )
}

test.setLengthZero <- function() {
	x <- NULL
	length(x) <- 0
	assertThat(x, identicalTo(NULL))
	
	length(x) <- 4999
	assertThat(x, identicalTo(NULL))
}