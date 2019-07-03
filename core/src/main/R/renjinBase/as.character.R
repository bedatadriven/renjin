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


# This needs to be evaluated BEFORE the normal

# Replace as.character() builtin with regular R function
# GNU R implements as.character() with very weird S3 dispatch
# that is better implemented here. That keeps the compiler simpler and requires
# hard-coding fewer exceptions


as.character.default <- function(x = NULL)
    as.vector(x, mode = "character")


as.character <- function(x, ...)
    UseMethod("as.character")
