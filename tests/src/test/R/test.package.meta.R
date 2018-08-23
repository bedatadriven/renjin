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


test.description <- function() {

    mf <- file.path(find.package("stats"), "Meta", "package.rds")
    m <- readRDS(mf)

    assertThat(m$DESCRIPTION['Package'], equalTo('stats'))
    assertThat(m$DESCRIPTION['Priority'], equalTo('base'))
}



test.affy <- function() {

    # This is actually wrong and probably not what the authors of the affy
    # package intended, but because there are more than two INDEX entries,
    # it will return NA instead of throwing an error
    INDEX <- library(help=stats)$info[[2]]

    assertThat(INDEX[[2]][2], identicalTo(NA_character_))
}

test.depends <- function() {

    mf <- file.path(find.package("graphics"), "Meta", "package.rds")
    m <- readRDS(mf)

    assertThat(m$Depends, identicalTo( list() ) )

}


test.library <- function() {

    assertThat( is.element(c("Version"),unlist(strsplit(library(help=utils)$info[[1]],":"))), identicalTo(TRUE))
    assertThat( library(help=utils)$name, identicalTo("utils"))
    assertThat( dir.exists(library(help=utils)$path), identicalTo(TRUE))
}
