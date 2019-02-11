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
assertThat(capabilities("jpeg"), identicalTo(c(jpeg=TRUE)))
assertThat(capabilities("png"), identicalTo(c(png=TRUE)))
assertThat(capabilities("tiff"), identicalTo(c(tiff=TRUE)))

assertThat(capabilities("tcltk"), identicalTo(c(tcltk=FALSE)))
assertThat(capabilities("X11"), identicalTo(c(X11=FALSE)))
assertThat(capabilities("aqua"), identicalTo(c(aqua=FALSE)))

assertThat(capabilities("http/ftp"), identicalTo(c("http/ftp"=TRUE)))
assertThat(capabilities("sockets"), identicalTo(c(sockets=TRUE)))

assertThat(capabilities("libxml"), identicalTo(c(libxml=FALSE)))

assertThat(capabilities("fifo"), identicalTo(c(fifo=TRUE)))

assertThat(capabilities("cledit"), identicalTo(c(cledit=FALSE)))

assertThat(capabilities("iconv"), identicalTo(c(iconv=TRUE)))
assertThat(capabilities("NLS"), identicalTo(c(NLS=FALSE)))

assertThat(capabilities("profmem"), identicalTo(c(profmem=FALSE)))
assertThat(capabilities("cairo"), identicalTo(c(cairo=FALSE)))
assertThat(capabilities("ICU"), identicalTo(c(ICU=FALSE)))
assertThat(capabilities("long.double"), identicalTo(c(long.double=FALSE)))
assertThat(capabilities("libcurl"), identicalTo(c(libcurl=TRUE)))

assertThat(capabilities(c("png", "png")), identicalTo(c(png=TRUE, png=TRUE)))
assertThat(capabilities("foobar"), identicalTo(structure(logical(0), names=character(0))))