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

ct <- as.POSIXct("2015-01-02 03:04:06.07", tz = "UTC")

assertThat(ct, identicalTo(structure(1420167846.07, class = c("POSIXct", "POSIXt"), tzone = "UTC")))

lt <- as.POSIXlt(ct)

assertThat(lt$sec, identicalTo(6.07))

assertTrue(identical(lt, structure(
    list(
        sec = 6.07,
        min = 4L,
        hour = 3L,
        mday = 2L,
        mon = 0L,
        year = 115L,
        wday = 5L,
        yday = 1L,
        isdst = 0L),
    class = c("POSIXlt",  "POSIXt"),
    tzone = "UTC")))

