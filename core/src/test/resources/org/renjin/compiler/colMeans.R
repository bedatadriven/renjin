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



colMeans <- function(x, na.rm = FALSE, dims = 1L)
{
	if(is.data.frame(x)) x <- as.matrix(x)
	if(!is.array(x) || length(dn <- dim(x)) < 2L)
		stop("'x' must be an array of at least two dimensions")
	if(dims < 1L || dims > length(dn) - 1L)
		stop("invalid 'dims'")
	n <- prod(dn[1L:dims])
	dn <- dn[-(1L:dims)]
	.Internal(colMeans(Re(x), n, prod(dn), na.rm)) +
						1i 
	z <- if(is.complex(x))
				.Internal(colMeans(Re(x), n, prod(dn), na.rm)) +
						1i * .Internal(colMeans(Im(x), n, prod(dn), na.rm))
			else .Internal(colMeans(x, n, prod(dn), na.rm))
	if(length(dn) > 1L) {
		dim(z) <- dn
		dimnames(z) <- dimnames(x)[-(1L:dims)]
	} else names(z) <- dimnames(x)[[dims+1]]
	z
}
