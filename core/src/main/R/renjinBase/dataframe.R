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


## RENJIN: internal representation of row.names as c(NA,-n)
## has been removed because sequences can now be efficiently
## represented as views of a sequence in Renjin

.row_names_info <- function(x, type = 1L) {
	if(type == 0) {
		attr(x, 'row.names')
	} else if( type >= 1L) {
	    if (type == 1L && identical(attr(x, 'row.names'), seq.int(from = 1L, nrow(x)))) {
	        # data.matrix relies on -n being returned in case row.names is set to the default values:
		    -length(attr(x, 'row.names'))
	    } else {
		    length(attr(x, 'row.names'))
		}
	} else {
		stop("invalid type argument")
	}
}

