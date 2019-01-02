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

Sys.setlocale('LC_COLLATE', 'C')

library(hamcrest)

test.zero.length.names <- function() {

    e <- list(1, 2, 3, 4)
    names(e) <- c("a", "b", "", NA)

    assertThat(eval(quote(sort(ls())), envir = e), identicalTo(c("NA", "a", "b")))
    assertThat(eval(quote(a+b), envir = e), identicalTo(3))
    assertThat(eval(quote(a+`NA`), envir = e), identicalTo(5))

}