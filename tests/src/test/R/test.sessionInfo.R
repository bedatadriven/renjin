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


# Many routines in the utils package expect the 'path' attribute
# on package environments
he <- as.environment("package:hamcrest")
print(he)
assertFalse(is.null(attr(he, "path")))

library(utils)
print(.Library)
print(file.path(.Library, "base"))
print(packageDescription("base"))

si <- sessionInfo()

hamcrest <- si$otherPkgs$hamcrest
str(hamcrest)

assertFalse(is.null(hamcrest))

hv <- hamcrest$Version

assertTrue(compareVersion('0.0.0',hv) < 0)