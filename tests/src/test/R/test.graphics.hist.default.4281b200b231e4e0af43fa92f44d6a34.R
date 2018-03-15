#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

 expected <- structure(list(breaks = c(0x1.4p+3, 0x1.ep+3, 0x1.4p+4, 0x1.9p+4, 
0x1.ep+4, 0x1.18p+5), counts = c(6L, 12L, 8L, 2L, 4L), density = c(0x1.3333333333333p-5, 
0x1.3333333333333p-4, 0x1.999999999999ap-5, 0x1.999999999999ap-7, 
0x1.999999999999ap-6), mids = c(0x1.9p+3, 0x1.18p+4, 0x1.68p+4, 
0x1.b8p+4, 0x1.04p+5), xname = "c(21, 21, 22.8, 21.4, 18.7, 18.1, 14.3, 24.4, 22.8, 19.2, 17.8, 16.4, 17.3, 15.2, 10.4, 10.4, 14.7, 32.4, 30.4, 33.9, 21.5, 15.5, 15.2, 13.3, 19.2, 27.3, 26, 30.4, 15.8, 19.7, 15, 21.4)", 
    equidist = TRUE), .Names = c("breaks", "counts", "density", 
"mids", "xname", "equidist"), class = "histogram") 
 

assertThat(graphics:::hist.default(plot=FALSE,x=c(21, 21, 22.8, 21.4, 18.7, 18.1, 14.3, 24.4, 22.8, 19.2, 17.8, 
16.4, 17.3, 15.2, 10.4, 10.4, 14.7, 32.4, 30.4, 33.9, 21.5, 15.5, 
15.2, 13.3, 19.2, 27.3, 26, 30.4, 15.8, 19.7, 15, 21.4))[-5]
,  identicalTo( expected[-5] ) )
