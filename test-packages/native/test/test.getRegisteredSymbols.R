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
library("org.renjin.test:native")

dlls <- getLoadedDLLs()

native.dll <- dlls$native

assertThat(native.dll, instanceOf("DLLInfo"))
assertThat(native.dll[["name"]], identicalTo("native"))

routines <- getDLLRegisteredRoutines("native")

assertThat(routines, instanceOf("DLLRegisteredRoutines"))
assertThat(routines$.C, identicalTo(structure(list(), class = "NativeRoutineList")))

assertThat(routines$.Call$Cmysum$name, identicalTo("Cmysum"))

assertThat(routines$.Fortran$Fdpchim$name, identicalTo("Fdpchim"))
assertThat(routines$.Fortran$Fdpchim$numParameters, identicalTo(0L))

