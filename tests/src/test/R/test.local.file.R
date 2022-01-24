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

test.file.resource <- function() {

    # The res: url schema is used to locate resources on the classpath.
    # it may resolve to file:/// uri or a jar:/// URI depending on where
    # the file is located.

    # in this case, the resource is part of the current build and so will resolve
    # to a file:/// URI.
    path <- normalizePath("res:mytestfile.txt", mustWork = TRUE)
    cat(sprintf("path = %s\n", path))

    # The result of local.file() should give an identical path
    local.path <- local.file(path)
    assertThat(local.path, equalTo(path))

}

test.jar.resource <- function() {
    print("test.jar.resource ")

    # in this case, the resource is part of the current build and so will resolve
    # to a file:/// URI.
    path <- normalizePath("res:org/renjin/sexp/AtomicVector.class", mustWork = TRUE)
    # jar:file:///home/ubuntu/renjin/core/build/libs/core-3.5-dev.jar!/org/renjin/sexp/AtomicVector.class

    cat(sprintf("\npath = %s\n", path))

    assertTrue(grepl(path, pattern = "^jar:"))

    # The result of local.file() should give an identical path
    local.path <- local.file(path)
    print(paste("local.path =", local.path))
    assertTrue(local.path != path)
    assertTrue(file.exists(local.path))

    # TODO: figure out what to do with the below case
    # path <- normalizePath("res:java/util/Map.class", mustWork = TRUE)
    # This will be jrt:///java.base/java/util/Map.class which will not resolve

}