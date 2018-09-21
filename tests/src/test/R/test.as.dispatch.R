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

as.vector.bar <- function(x, mode) 90L
as.vector.baz <- function(x, mode) 91L

as.raw.baz <- function(x) 91L
as.logical.baz <- function(x) 92L
as.integer.baz <- function(x) 93L
as.double.baz <- function(x) 94L
as.numeric.baz <- function(x) 95L
as.complex.baz <- function(x) 96L
as.character.baz <- function(x) 97L
as.list.baz <- function(x) 98L
as.name.baz <- function(x) 99L

as.raw.zarb <- function(x) 391L
as.logical.zarb <- function(x) 392L
as.integer.zarb <- function(x) 393L
as.double.zarb <- function(x) 394L
as.numeric.zarb <- function(x) 395L
as.complex.zarb <- function(x) 396L
as.character.zarb <- function(x) 397L
as.list.zarb <- function(x) 398L
as.name.zarb <- function(x) 399L


# What if there is a generic defined for as.vector but not the individual as.xxx method?
# Only as.character and as.list delegate to as.vector
bar <- structure(42L, class="bar")

assertThat(as.raw(bar), identicalTo(as.raw(0x2a)))
assertThat(as.logical(bar), identicalTo(TRUE))
assertThat(as.integer(bar), identicalTo(42L))
assertThat(as.double(bar), identicalTo(42))
assertThat(as.numeric(bar), identicalTo(42))
assertThat(as.complex(bar), identicalTo(42+0i))

assertThat(as.character(bar), identicalTo(90L))
assertThat(as.list(bar), identicalTo(90L))
assertThat(as.name(bar), identicalTo(90L))


# Check dispatch to generic methods for the individual xx methods
# There is no as.vector method defined

zarb <- structure(43L, class="zarb")

assertThat(as.raw(zarb), identicalTo(391L))
assertThat(as.logical(zarb), identicalTo(392L))
assertThat(as.integer(zarb), identicalTo(393L))
assertThat(as.double(zarb), identicalTo(394L))
assertThat(as.numeric(zarb), identicalTo(394L))
assertThat(as.complex(zarb), identicalTo(396L))
assertThat(as.character(zarb), identicalTo(397L))
assertThat(as.list(zarb), identicalTo(398L))

# What if both are defined? Which one wins out?

baz <- structure(41L, class="baz")
assertThat(as.raw(baz), identicalTo(91L))
assertThat(as.logical(baz), identicalTo(92L))
assertThat(as.integer(baz), identicalTo(93L))
assertThat(as.double(baz), identicalTo(94L))
assertThat(as.numeric(baz), identicalTo(94L))
assertThat(as.complex(baz), identicalTo(96L))
assertThat(as.character(baz), identicalTo(97L))
assertThat(as.list(baz), identicalTo(98L))