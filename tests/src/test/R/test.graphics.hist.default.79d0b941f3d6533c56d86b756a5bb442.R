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

library(hamcrest)

 expected <- structure(list(breaks = c(0x1.4p+3, 0x1.8p+3, 0x1.cp+3, 0x1p+4, 
0x1.2p+4, 0x1.4p+4, 0x1.6p+4, 0x1.8p+4, 0x1.ap+4, 0x1.cp+4, 0x1.ep+4, 
0x1p+5, 0x1.1p+5), counts = c(2L, 1L, 7L, 3L, 5L, 5L, 2L, 2L, 
1L, 0L, 2L, 2L), density = c(0x1p-5, 0x1p-6, 0x1.cp-4, 0x1.8p-5, 
0x1.4p-4, 0x1.4p-4, 0x1p-5, 0x1p-5, 0x1p-6, 0x0p+0, 0x1p-5, 0x1p-5
), mids = c(0x1.6p+3, 0x1.ap+3, 0x1.ep+3, 0x1.1p+4, 0x1.3p+4, 
0x1.5p+4, 0x1.7p+4, 0x1.9p+4, 0x1.bp+4, 0x1.dp+4, 0x1.fp+4, 0x1.08p+5
), xname = "c(21, 21, 22.8, 21.4, 18.7, 18.1, 14.3, 24.4, 22.8, 19.2, 17.8, 16.4, 17.3, 15.2, 10.4, 10.4, 14.7, 32.4, 30.4, 33.9, 21.5, 15.5, 15.2, 13.3, 19.2, 27.3, 26, 30.4, 15.8, 19.7, 15, 21.4)", 
    equidist = TRUE), .Names = c("breaks", "counts", "density", 
"mids", "xname", "equidist"), class = "histogram") 


assertThat(graphics:::hist.default(breaks=10,col="red",main="Histogram with Normal Curve",plot=FALSE,x=c(21, 21, 22.8, 21.4, 18.7, 18.1, 14.3, 24.4, 22.8, 19.2, 17.8, 
16.4, 17.3, 15.2, 10.4, 10.4, 14.7, 32.4, 30.4, 33.9, 21.5, 15.5, 
15.2, 13.3, 19.2, 27.3, 26, 30.4, 15.8, 19.7, 15, 21.4),xlab="Miles Per Gallon")[-5]
,  identicalTo( expected[-5] ) )
