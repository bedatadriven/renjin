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

 expected <- structure(list(breaks = c(0x0p+0, 0x1.f4p+10, 0x1.f4p+11, 0x1.77p+12, 
0x1.f4p+12, 0x1.388p+13, 0x1.77p+13, 0x1.b58p+13, 0x1.f4p+13, 
0x1.194p+14), counts = c(41L, 2L, 1L, 1L, 1L, 1L, 0L, 0L, 1L), 
    density = c(0x1.bfd44f3078264p-12, 0x1.5d867c3ece2a5p-16, 
    0x1.5d867c3ece2a5p-17, 0x1.5d867c3ece2a5p-17, 0x1.5d867c3ece2a5p-17, 
    0x1.5d867c3ece2a5p-17, 0x0p+0, 0x0p+0, 0x1.5d867c3ece2a5p-17
    ), mids = c(0x1.f4p+9, 0x1.77p+11, 0x1.388p+12, 0x1.b58p+12, 
    0x1.194p+13, 0x1.57cp+13, 0x1.964p+13, 0x1.d4cp+13, 0x1.09ap+14
    ), xname = "structure(c(11506, 5500, 16988, 2968, 16, 184, 23, 280, 84, 73, 25, 43, 21, 82, 3745, 840, 13, 30, 30, 89, 40, 33, 49, 14, 42, 227, 16, 36, 29, 15, 306, 44, 58, 43, 9390, 32, 13, 29, 6795, 16, 15, 183, 14, 26, 19, 13, 12, 82), .Names = c(\"Africa\", \"Antarctica\", \"Asia\", \"Australia\", \"Axel Heiberg\", \"Baffin\", \"Banks\", \"Borneo\", \"Britain\", \"Celebes\", \"Celon\", \"Cuba\", \"Devon\", \"Ellesmere\", \"Europe\", \"Greenland\", \"Hainan\", \"Hispaniola\", \"Hokkaido\", \"Honshu\", \"Iceland\", \"Ireland\", \"Java\", \"Kyushu\", \"Luzon\", \n    \"Madagascar\", \"Melville\", \"Mindanao\", \"Moluccas\", \"New Britain\", \"New Guinea\", \"New Zealand (N)\", \"New Zealand (S)\", \"Newfoundland\", \"North America\", \"Novaya Zemlya\", \"Prince of Wales\", \"Sakhalin\", \"South America\", \"Southampton\", \"Spitsbergen\", \"Sumatra\", \"Taiwan\", \"Tasmania\", \"Tierra del Fuego\", \"Timor\", \"Vancouver\", \"Victoria\"))", 
    equidist = TRUE), .Names = c("breaks", "counts", "density", 
"mids", "xname", "equidist"), class = "histogram") 
 

assertThat(graphics:::hist.default(x=structure(c(11506, 5500, 16988, 2968, 16, 184, 23, 280, 84, 73, 
25, 43, 21, 82, 3745, 840, 13, 30, 30, 89, 40, 33, 49, 14, 42, 
227, 16, 36, 29, 15, 306, 44, 58, 43, 9390, 32, 13, 29, 6795, 
16, 15, 183, 14, 26, 19, 13, 12, 82), .Names = c("Africa", "Antarctica", 
"Asia", "Australia", "Axel Heiberg", "Baffin", "Banks", "Borneo", 
"Britain", "Celebes", "Celon", "Cuba", "Devon", "Ellesmere", 
"Europe", "Greenland", "Hainan", "Hispaniola", "Hokkaido", "Honshu", 
"Iceland", "Ireland", "Java", "Kyushu", "Luzon", "Madagascar", 
"Melville", "Mindanao", "Moluccas", "New Britain", "New Guinea", 
"New Zealand (N)", "New Zealand (S)", "Newfoundland", "North America", 
"Novaya Zemlya", "Prince of Wales", "Sakhalin", "South America", 
"Southampton", "Spitsbergen", "Sumatra", "Taiwan", "Tasmania", 
"Tierra del Fuego", "Timor", "Vancouver", "Victoria")),plot=FALSE)[-5]
,  identicalTo( expected[-5] ) )
