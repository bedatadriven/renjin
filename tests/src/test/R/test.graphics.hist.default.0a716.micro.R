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

 expected <- structure(
     list(
         breaks = c(0x1.9p+6, 0x1.2cp+8, 0x1.f4p+8, 0x1.5ep+9),
         counts = c(12L, 9L, 3L),
         density = c(0x1.47ae147ae147bp-9, 0x1.eb851eb851eb8p-10, 0x1.47ae147ae147bp-11),
         mids = c(0x1.9p+7, 0x1.9p+8, 0x1.2cp+9),
         xname = "structure(c(112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118, 463, 407, 362, 405, 417, 391, 419, 461, 472, 535, 622, 606), .Tsp = c(1949, 1950.91666666667, 12), class = \"ts\")",
         equidist = TRUE
         ),
     .Names = c("breaks", "counts", "density", "mids", "xname", "equidist"),
     class = "histogram"
     )


assertThat(graphics:::hist.default(breaks=c(100, 300, 500, 700),plot=FALSE,x=structure(c(
112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
463, 407, 362, 405, 417, 391, 419, 461, 472, 535, 622, 606
), .Tsp = c(1949, 1950.91666666667, 12), class = "ts"))[-5]
,  identicalTo( expected[-5] ) )